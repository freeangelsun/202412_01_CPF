package com.cpf.batch.scheduler;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.spi.BatchApprovedLaunchRequestResolver;
import com.cpf.batch.scheduler.internal.JdbcSchedulerLeaderRepository;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DB Scheduler의 durable launch-outbox dispatcher입니다.
 *
 * <p>Schedule advance와 launch command 생성은 하나의 DB transaction으로 확정하고, 실제 Spring Batch
 * 시작은 transaction 밖에서 수행합니다. 외부 시작 결과가 불명확하면 UNKNOWN으로 남겨 동일
 * idempotency key로 재조정하므로 DB rollback 뒤 중복 Job 시작이 발생하지 않습니다.</p>
 */
@Component
public class SchedulerDispatchService {
    private static final int DISPATCH_BATCH_SIZE = 100;

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
        List<Map<String, Object>> due = jdbc.queryForList(sql.required("scheduler-find-due"));
        for (Map<String, Object> row : due) {
            transaction.executeWithoutResult(status -> stage(row, lease));
        }
        dispatchPending(lease);
    }

    /** 재시작 또는 앞선 UNKNOWN 결과까지 포함해 durable command를 재조정합니다. */
    public void dispatchPending() {
        if (!runtimeEnabled()) {
            return;
        }
        long fencingToken = coordinator.fencingToken();
        if (fencingToken <= 0) {
            return;
        }
        dispatchPending(coordinator.assertLeader(fencingToken));
    }

    private void stage(Map<String, Object> row, JdbcSchedulerLeaderRepository.Lease lease) {
        coordinator.assertLeader(lease.fencingToken());
        String scheduleId = requiredText(row, "schedule_id");
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
        if (businessDayOnly && !calendarRuntimeEnabled()) {
            return;
        }
        if (businessDayOnly && !calendar.isBusinessDay(calendarId, businessDate)) {
            if ("NEXT_BUSINESS_DAY".equalsIgnoreCase(Objects.toString(row.get("holiday_policy"), "SKIP"))) {
                businessDate = calendar.nextBusinessDay(calendarId, businessDate, 1);
            } else {
                advance(row, fireAt, lease);
                return;
            }
        }

        Timestamp scheduledAt = Timestamp.from(fireAt.toInstant());
        String idempotencyKey = scheduleId + ":" + scheduledAt.toInstant();
        int inserted;
        try {
            inserted = jdbc.update(sql.required("scheduler-trigger-insert-fenced"),
                    scheduleId, scheduledAt, lease.fencingToken(), jobId, definitionVersion,
                    definitionChecksum, java.sql.Date.valueOf(businessDate), zone.getId(), idempotencyKey,
                    SchedulerCoordinator.LEASE_KEY, lease.instanceId(), lease.fencingToken());
        } catch (DuplicateKeyException duplicateTrigger) {
            inserted = 0;
        }
        if (inserted == 0) {
            Integer existing = jdbc.queryForObject(sql.required("scheduler-trigger-count"),
                    Integer.class, scheduleId, scheduledAt);
            if (existing == null || existing == 0) {
                throw new IllegalStateException("Stale scheduler leader fenced before trigger insert");
            }
        }
        advance(row, fireAt, lease);
    }

    private void dispatchPending(JdbcSchedulerLeaderRepository.Lease lease) {
        coordinator.assertLeader(lease.fencingToken());
        List<Map<String, Object>> commands = jdbc.queryForList(
                sql.required("scheduler-trigger-find-dispatchable"), DISPATCH_BATCH_SIZE);
        for (Map<String, Object> command : commands) {
            coordinator.assertLeader(lease.fencingToken());
            String scheduleId = requiredText(command, "schedule_id");
            Timestamp scheduledAt = requiredTimestamp(command, "scheduled_fire_at");
            boolean claimed = Boolean.TRUE.equals(transaction.execute(status ->
                    jdbc.update(sql.required("scheduler-trigger-claim"),
                            lease.instanceId(), lease.fencingToken(),
                            scheduleId, scheduledAt, SchedulerCoordinator.LEASE_KEY,
                            lease.instanceId(), lease.fencingToken()) == 1));
            if (!claimed) {
                continue;
            }
            dispatchClaimed(command, lease, scheduleId, scheduledAt);
        }
    }

    private void dispatchClaimed(
            Map<String, Object> command,
            JdbcSchedulerLeaderRepository.Lease lease,
            String scheduleId,
            Timestamp scheduledAt) {
        try {
            ZoneId zone = ZoneId.of(requiredText(command, "fire_zone"));
            ZonedDateTime fireAt = scheduledAt.toInstant().atZone(zone);
            BatchExecutionLink execution = executionControl.start(launchRequestResolver.resolve(
                    new BatchApprovedLaunchRequestResolver.TriggerContext(
                            scheduleId,
                            requiredText(command, "job_id"),
                            requiredLong(command, "definition_version"),
                            requiredText(command, "definition_checksum"),
                            requiredDate(command, "business_date"),
                            fireAt.toOffsetDateTime(),
                            requiredLong(command, "fencing_token"),
                            requiredText(command, "idempotency_key"))));
            if (execution.jobExecutionId() == null) {
                throw new IllegalStateException("SPRING_BATCH_EXECUTION_ID_MISSING");
            }
            int changed = transaction.execute(status -> jdbc.update(
                    sql.required("scheduler-trigger-mark-dispatched"),
                    execution.jobExecutionId(), scheduleId, scheduledAt,
                    lease.instanceId(), lease.fencingToken()));
            if (changed != 1) {
                throw new IllegalStateException("SCHEDULER_DISPATCH_FENCED_AFTER_START");
            }
        } catch (RuntimeException failure) {
            transaction.executeWithoutResult(status -> jdbc.update(
                    sql.required("scheduler-trigger-mark-unknown"),
                    failureCode(failure), scheduleId, scheduledAt,
                    lease.instanceId(), lease.fencingToken()));
        }
    }

    private void advance(Map<String, Object> row, ZonedDateTime from, JdbcSchedulerLeaderRepository.Lease lease) {
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

    private static String failureCode(Throwable failure) {
        String simple = failure.getClass().getSimpleName();
        return simple.length() <= 100 ? simple : simple.substring(0, 100);
    }

    private static String requiredText(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        String value = raw == null ? "" : raw.toString().trim();
        if (value.isEmpty()) throw new IllegalStateException("Scheduler projection is missing " + key);
        return value;
    }

    private static long requiredLong(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        if (raw instanceof Number number) return number.longValue();
        try { return Long.parseLong(requiredText(row, key)); }
        catch (NumberFormatException invalid) {
            throw new IllegalStateException("Scheduler projection has invalid " + key, invalid);
        }
    }

    private static Timestamp requiredTimestamp(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        if (raw instanceof Timestamp timestamp) return timestamp;
        if (raw instanceof LocalDateTime value) return Timestamp.valueOf(value);
        return Timestamp.valueOf(requiredText(row, key).replace('T', ' '));
    }

    private static LocalDate requiredDate(Map<String, Object> row, String key) {
        Object raw = row.get(key);
        if (raw instanceof java.sql.Date value) return value.toLocalDate();
        if (raw instanceof LocalDate value) return value;
        return LocalDate.parse(requiredText(row, key));
    }

    static boolean withinAvailableWindow(LocalTime value, LocalTime start, LocalTime end) {
        if (start == null || end == null) return true;
        if (!start.isAfter(end)) return !value.isBefore(start) && !value.isAfter(end);
        return !value.isBefore(start) || !value.isAfter(end);
    }

    private static LocalTime toLocalTime(Object value) {
        if (value == null) return null;
        if (value instanceof LocalTime localTime) return localTime;
        if (value instanceof Time time) return time.toLocalTime();
        return LocalTime.parse(value.toString());
    }

    private static ZonedDateTime toZonedDateTime(Object value, ZoneId zone) {
        if (value == null) throw new IllegalStateException("Scheduler next_fire_at is required");
        if (value instanceof Timestamp timestamp) return timestamp.toInstant().atZone(zone);
        if (value instanceof LocalDateTime localDateTime) return localDateTime.atZone(zone);
        return LocalDateTime.parse(value.toString().replace(' ', 'T')).atZone(zone);
    }
}
