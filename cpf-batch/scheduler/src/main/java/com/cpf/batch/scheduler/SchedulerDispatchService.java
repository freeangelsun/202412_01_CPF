package com.cpf.batch.scheduler;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class SchedulerDispatchService {
    private final SchedulerCoordinator coordinator;
    private final JdbcTemplate jdbc;
    private final CmnBusinessCalendar calendar;
    private final TransactionTemplate transaction;
    private final BatchExecutionControlPort executionControl;
    private final BatchApprovedLaunchRequestResolver launchRequestResolver;
    private final CpfVendorSqlCatalog sql;
    private volatile BatchRuntimePolicy runtimePolicy = new BatchRuntimePolicy();

    public SchedulerDispatchService(
            SchedulerCoordinator coordinator,
            JdbcTemplate jdbc,
            CmnBusinessCalendar calendar,
            PlatformTransactionManager transactionManager,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            BatchExecutionControlPort executionControl,
            BatchApprovedLaunchRequestResolver launchRequestResolver) {
        this.coordinator = coordinator;
        this.jdbc = jdbc;
        this.calendar = calendar;
        this.transaction = new TransactionTemplate(transactionManager);
        this.sql = sqlCatalogProvider.forModule("bat");
        this.executionControl = executionControl;
        this.launchRequestResolver = launchRequestResolver;
    }

    /** 기존 생성자 기반 Test/Consumer를 깨지 않으면서 공통 Runtime 정책을 실제 dispatch gate에 연결합니다. */
    @Autowired
    public void setRuntimePolicy(BatchRuntimePolicy runtimePolicy) {
        this.runtimePolicy = Objects.requireNonNull(runtimePolicy, "runtimePolicy");
    }

    boolean runtimeEnabled() {
        return runtimePolicy.current().schedulerEnabled();
    }

    boolean calendarRuntimeEnabled() {
        return runtimePolicy.current().calendarEnabled();
    }

    public void dispatchDue() {
        if (!runtimeEnabled()) {
            return;
        }
        long fencingToken = coordinator.fencingToken();
        if (fencingToken <= 0) {
            return;
        }
        JdbcSchedulerLeaderRepository.Lease lease = coordinator.assertLeader(fencingToken);
        List<Map<String, Object>> due =
                jdbc.queryForList(sql.required("scheduler-find-due"));
        for (Map<String, Object> row : due) {
            transaction.executeWithoutResult(status -> fire(row, lease));
        }
    }

    private void fire(Map<String, Object> row, JdbcSchedulerLeaderRepository.Lease lease) {
        coordinator.assertLeader(lease.fencingToken());
        String scheduleId = String.valueOf(row.get("schedule_id"));
        String jobId = requiredText(row, "job_id");
        long definitionVersion = requiredLong(row, "definition_version");
        String definitionChecksum = requiredText(row, "definition_checksum");
        ZoneId zone = ZoneId.of(requiredText(row, "timezone"));
        ZonedDateTime fireAt = toZonedDateTime(row.get("next_fire_at"), zone);

        if (!withinAvailableWindow(
                fireAt.toLocalTime(), toLocalTime(row.get("available_start_time")),
                toLocalTime(row.get("available_end_time")))) {
            advance(row, fireAt, lease);
            return;
        }

        LocalDate businessDate = fireAt.toLocalDate();
        String calendarId = Objects.toString(row.get("calendar_id"), "DEFAULT");
        boolean businessDayOnly = "Y".equals(row.get("business_day_only_yn"));
        // Calendar 정책 중지 중에는 영업일 의존 일정의 next_fire_at을 소진하지 않고 보류합니다.
        if (businessDayOnly && !calendarRuntimeEnabled()) {
            return;
        }
        if (businessDayOnly && !calendar.isBusinessDay(calendarId, businessDate)) {
            if ("NEXT_BUSINESS_DAY".equalsIgnoreCase(
                    Objects.toString(row.get("holiday_policy"), "SKIP"))) {
                businessDate = calendar.nextBusinessDay(calendarId, businessDate, 1);
            } else {
                advance(row, fireAt, lease);
                return;
            }
        }

        Timestamp scheduledAt = Timestamp.from(fireAt.toInstant());
        int inserted;
        try {
            inserted = jdbc.update(sql.required("scheduler-trigger-insert-fenced"),
                    scheduleId, scheduledAt, lease.fencingToken(), SchedulerCoordinator.LEASE_KEY,
                    lease.instanceId(), lease.fencingToken());
        } catch (DuplicateKeyException duplicateTrigger) {
            inserted = 0;
        }

        if (inserted == 1) {
            String idempotencyKey = scheduleId + ":" + scheduledAt.toInstant();
            BatchExecutionLink execution = executionControl.start(launchRequestResolver.resolve(
                    new BatchApprovedLaunchRequestResolver.TriggerContext(
                            scheduleId, jobId, definitionVersion, definitionChecksum, businessDate,
                            fireAt.toOffsetDateTime(), lease.fencingToken(), idempotencyKey)));
            if (execution.jobExecutionId() == null) {
                throw new IllegalStateException("Spring Batch JobExecution identity was not returned");
            }
            jdbc.update(sql.required("scheduler-trigger-mark-dispatched"),
                    execution.jobExecutionId(), scheduleId, scheduledAt, lease.fencingToken());
        } else {
            Integer existing = jdbc.queryForObject(sql.required("scheduler-trigger-count"),
                    Integer.class, scheduleId, scheduledAt);
            if (existing == null || existing == 0) {
                throw new IllegalStateException("Stale scheduler leader fenced before trigger insert");
            }
        }
        advance(row, fireAt, lease);
    }

    private void advance(
            Map<String, Object> row,
            ZonedDateTime from,
            JdbcSchedulerLeaderRepository.Lease lease) {
        coordinator.assertLeader(lease.fencingToken());
        ZonedDateTime next = CronExpression.parse(String.valueOf(row.get("cron_expression"))).next(from);
        if (next == null) {
            throw new IllegalStateException("No next cron fire");
        }
        Timestamp previous = row.get("next_fire_at") == null
                ? null : Timestamp.from(toZonedDateTime(row.get("next_fire_at"), from.getZone()).toInstant());
        int changed = jdbc.update(sql.required("scheduler-advance"),
                Timestamp.from(from.toInstant()), Timestamp.from(next.toInstant()), row.get("schedule_id"),
                previous, previous, SchedulerCoordinator.LEASE_KEY, lease.instanceId(), lease.fencingToken());
        if (changed == 0) {
            throw new IllegalStateException("Schedule advance rejected by fencing or concurrent update");
        }
    }

    private static String requiredText(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        String value = raw == null ? "" : raw.toString().trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Scheduler projection is missing " + key);
        }
        return value;
    }

    private static long requiredLong(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        if (raw instanceof Number number) {
            return number.longValue();
        }
        String value = raw == null ? "" : raw.toString().trim();
        if (value.isEmpty()) {
            throw new IllegalStateException("Scheduler projection is missing " + key);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException invalid) {
            throw new IllegalStateException("Scheduler projection has invalid " + key + ": " + value, invalid);
        }
    }

    static boolean withinAvailableWindow(LocalTime value, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return true;
        }
        if (!start.isAfter(end)) {
            return !value.isBefore(start) && !value.isAfter(end);
        }
        return !value.isBefore(start) || !value.isAfter(end);
    }

    private static LocalTime toLocalTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value instanceof Time time) {
            return time.toLocalTime();
        }
        return LocalTime.parse(value.toString());
    }

    private static ZonedDateTime toZonedDateTime(Object value, ZoneId zone) {
        if (value == null) {
            throw new IllegalStateException("Scheduler next_fire_at is required");
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant().atZone(zone);
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.atZone(zone);
        }
        return LocalDateTime.parse(value.toString().replace(' ', 'T')).atZone(zone);
    }
}
