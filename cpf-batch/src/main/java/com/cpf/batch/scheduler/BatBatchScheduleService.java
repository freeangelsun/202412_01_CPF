package com.cpf.batch.scheduler;

import com.cpf.common.calendar.CmnBusinessCalendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * CPF 배치 스케줄의 실행 가능 여부를 계산합니다.
 *
 * <p>cron, 영업일, 수행 가능 시간, 실행일 패턴을 한 곳에서 판정해 ADM 시뮬레이션과
 * 자동 스케줄러가 같은 기준으로 동작하도록 유지합니다.</p>
 */
@Service
public class BatBatchScheduleService {
    private static final Logger log = LoggerFactory.getLogger(BatBatchScheduleService.class);
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final JdbcTemplate batJdbcTemplate;
    private final CmnBusinessCalendar businessCalendar;

    /**
     * BAT는 영업일 저장소를 직접 소유하지 않고 CMN Calendar 계약만 소비합니다.
     */
    public BatBatchScheduleService(
            @Qualifier("batJdbcTemplate") JdbcTemplate batJdbcTemplate,
            CmnBusinessCalendar businessCalendar) {
        this.batJdbcTemplate = batJdbcTemplate;
        this.businessCalendar = businessCalendar;
    }

    public List<BatBatchScheduleCandidate> findDueSchedules(LocalDateTime serverNow) {
        List<Map<String, Object>> schedules = batJdbcTemplate.queryForList("""
                SELECT s.schedule_id, s.job_id, s.cron_expression, s.timezone, s.calendar_id,
                       s.business_day_only_yn, s.available_start_time, s.available_end_time,
                       s.run_date_pattern, s.last_fire_at, s.next_fire_at
                FROM bat_schedule s
                JOIN bat_job j ON j.job_id = s.job_id
                WHERE s.enabled_yn = 'Y'
                  AND j.use_yn = 'Y'
                ORDER BY s.schedule_id
                """);
        List<BatBatchScheduleCandidate> dueSchedules = new ArrayList<>();
        for (Map<String, Object> schedule : schedules) {
            buildCandidate(schedule, serverNow).ifPresent(dueSchedules::add);
        }
        return dueSchedules;
    }

    public void updateFireTimes(BatBatchScheduleCandidate candidate, String requestUser) {
        batJdbcTemplate.update("""
                UPDATE bat_schedule
                SET last_fire_at = ?,
                    next_fire_at = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE schedule_id = ?
                """,
                candidate.plannedRunAt(),
                candidate.nextRunAt(),
                defaultIfBlank(requestUser, "CpfBatchScheduler"),
                candidate.scheduleId());
    }

    private java.util.Optional<BatBatchScheduleCandidate> buildCandidate(
            Map<String, Object> schedule,
            LocalDateTime serverNow) {
        try {
            String timezone = defaultIfBlank(value(schedule.get("timezone")), "Asia/Seoul");
            ZoneId zoneId = ZoneId.of(timezone);
            ZonedDateTime now = serverNow.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
            LocalDate businessDate = now.toLocalDate();

            if (!isWithinAvailableWindow(schedule, now.toLocalTime())) {
                return java.util.Optional.empty();
            }
            if (!matchesRunDatePattern(value(schedule.get("run_date_pattern")), businessDate)) {
                return java.util.Optional.empty();
            }
            if ("Y".equalsIgnoreCase(value(schedule.get("business_day_only_yn")))
                    && !isBusinessDay(value(schedule.get("calendar_id")), businessDate)) {
                return java.util.Optional.empty();
            }

            CronExpression cronExpression = CronExpression.parse(value(schedule.get("cron_expression")));
            ZonedDateTime base = resolveBaseTime(schedule, now, zoneId);
            ZonedDateTime planned = cronExpression.next(base);
            if (planned == null || planned.isAfter(now)) {
                return java.util.Optional.empty();
            }
            ZonedDateTime next = cronExpression.next(planned);
            String scheduleId = value(schedule.get("schedule_id"));
            String jobId = value(schedule.get("job_id"));
            String jobParameters = """
                    {"scheduleId":"%s","plannedRunAt":"%s","businessDate":"%s"}
                    """.formatted(scheduleId, planned.toLocalDateTime(), businessDate).trim();
            return java.util.Optional.of(new BatBatchScheduleCandidate(
                    scheduleId,
                    jobId,
                    value(schedule.get("cron_expression")),
                    timezone,
                    defaultIfBlank(value(schedule.get("calendar_id")), "DEFAULT"),
                    planned.toLocalDateTime(),
                    next == null ? null : next.toLocalDateTime(),
                    businessDate,
                    jobParameters));
        } catch (Exception ex) {
            log.warn("배치 스케줄 실행 가능 여부 계산을 건너뜁니다. schedule={}, message={}", schedule, ex.getMessage());
            return java.util.Optional.empty();
        }
    }

    private ZonedDateTime resolveBaseTime(Map<String, Object> schedule, ZonedDateTime now, ZoneId zoneId) {
        LocalDateTime nextFireAt = toLocalDateTime(schedule.get("next_fire_at"));
        if (nextFireAt != null && !nextFireAt.atZone(zoneId).isAfter(now)) {
            return nextFireAt.minusNanos(1).atZone(zoneId);
        }
        LocalDateTime lastFireAt = toLocalDateTime(schedule.get("last_fire_at"));
        if (lastFireAt != null) {
            return lastFireAt.atZone(zoneId);
        }
        return now.minusMinutes(1);
    }

    private boolean isWithinAvailableWindow(Map<String, Object> schedule, LocalTime now) {
        LocalTime start = toLocalTime(schedule.get("available_start_time"));
        LocalTime end = toLocalTime(schedule.get("available_end_time"));
        if (start == null && end == null) {
            return true;
        }
        if (start != null && end == null) {
            return !now.isBefore(start);
        }
        if (start == null) {
            return !now.isAfter(end);
        }
        if (start.equals(end)) {
            return true;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && !now.isAfter(end);
        }
        return !now.isBefore(start) || !now.isAfter(end);
    }

    private boolean matchesRunDatePattern(String pattern, LocalDate businessDate) {
        if (!hasText(pattern) || "*".equals(pattern.trim())) {
            return true;
        }
        String normalized = pattern.replace(" ", "");
        String basic = businessDate.format(BASIC_DATE);
        String iso = businessDate.toString();
        for (String token : normalized.split(",")) {
            if (token.equals(basic) || token.equals(iso)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBusinessDay(String calendarId, LocalDate businessDate) {
        return businessCalendar.isBusinessDay(defaultIfBlank(calendarId, "DEFAULT"), businessDate);
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value != null && hasText(String.valueOf(value))) {
            return LocalDateTime.parse(String.valueOf(value).replace(" ", "T"));
        }
        return null;
    }

    private LocalTime toLocalTime(Object value) {
        if (value instanceof Time time) {
            return time.toLocalTime();
        }
        if (value instanceof LocalTime localTime) {
            return localTime;
        }
        if (value != null && hasText(String.valueOf(value))) {
            return LocalTime.parse(String.valueOf(value));
        }
        return null;
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static boolean hasText(String v){return v!=null&&!v.isBlank();}
    private static String defaultIfBlank(String v,String d){return hasText(v)?v.trim():d;}
    private static String requireText(String v,String n){if(!hasText(v))throw new IllegalArgumentException(n+"은(는) 필수입니다.");return v.trim();}

}
