package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW 배치 운영 메타와 Spring Batch 실행 기능을 연결합니다.
 *
 * <p>실제 Spring Batch Job bean이 있으면 JobLauncher와 JobOperator로 실행, 재시작, 중지를 수행합니다.
 * 로컬 교육 환경처럼 Job bean이 없는 경우에도 ADM 운영 메타 테이블에는 요청 이력을 남겨 화면 검증이 가능하게 합니다.</p>
 */
@Service
public class AdmBatchOperationService {
    private static final Logger log = LoggerFactory.getLogger(AdmBatchOperationService.class);

    private final JdbcTemplate pfwJdbcTemplate;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final JobOperator jobOperator;
    private final Map<String, Job> jobs;

    public AdmBatchOperationService(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            ObjectProvider<JobLauncher> jobLauncherProvider,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            ObjectProvider<JobOperator> jobOperatorProvider,
            ObjectProvider<Map<String, Job>> jobsProvider) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.jobLauncher = jobLauncherProvider.getIfAvailable();
        this.jobExplorer = jobExplorerProvider.getIfAvailable();
        this.jobOperator = jobOperatorProvider.getIfAvailable();
        this.jobs = jobsProvider.getIfAvailable(Map::of);
    }

    public List<Map<String, Object>> findJobs() {
        return queryOrEmpty("""
                SELECT j.job_id, j.job_name, j.job_type, j.description, j.restartable_yn, j.use_yn,
                       MAX(e.start_time) AS last_start_time,
                       MAX(e.end_time) AS last_end_time,
                       SUM(CASE WHEN e.execution_status = 'COMPLETED' THEN 1 ELSE 0 END) AS success_count,
                       SUM(CASE WHEN e.execution_status IN ('FAILED', 'STOPPED') THEN 1 ELSE 0 END) AS failure_count,
                       AVG(TIMESTAMPDIFF(SECOND, e.start_time, e.end_time)) AS avg_duration_seconds
                FROM pfw_batch_job j
                LEFT JOIN pfw_batch_execution e ON e.job_id = j.job_id
                WHERE j.use_yn = 'Y'
                GROUP BY j.job_id, j.job_name, j.job_type, j.description, j.restartable_yn, j.use_yn
                ORDER BY j.job_id
                """);
    }

    public List<Map<String, Object>> findSchedules() {
        return queryOrEmpty("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM pfw_batch_schedule
                ORDER BY job_id, schedule_id
                """);
    }

    public List<Map<String, Object>> findExecutions(String jobId, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                           spring_batch_execution_id, batch_instance_id,
                           start_time, end_time, read_count, write_count, skip_count,
                           error_message, requested_by, created_at, updated_at
                    FROM pfw_batch_execution
                    WHERE job_id = ?
                    ORDER BY execution_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryOrEmpty("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, batch_instance_id,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, requested_by, created_at, updated_at
                FROM pfw_batch_execution
                ORDER BY execution_id DESC
                LIMIT ?
                """, resolvedLimit);
    }

    public Map<String, Object> findExecutionDetail(long executionId) {
        try {
            Map<String, Object> execution = findExecution(executionId);
            List<Map<String, Object>> steps = pfwJdbcTemplate.queryForList("""
                    SELECT step_execution_id, execution_id, step_name, execution_status,
                           start_time, end_time, read_count, write_count, skip_count,
                           error_message, step_log, created_at, updated_at
                    FROM pfw_batch_step_execution
                    WHERE execution_id = ?
                    ORDER BY step_execution_id
                    """, executionId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("execution", execution);
            result.put("steps", steps);
            result.put("springBatch", findSpringBatchExecution(execution));
            return result;
        } catch (DataAccessException ex) {
            log.debug("배치 실행 상세 조회를 건너뜁니다. executionId={}, reason={}", executionId, ex.getMessage());
            return Map.of();
        }
    }

    public List<Map<String, Object>> findInstances() {
        return queryOrEmpty("""
                SELECT instance_id, instance_name, host_name, server_port, active_yn,
                       last_heartbeat_at, description, created_at, updated_at
                FROM pfw_batch_instance
                ORDER BY active_yn DESC, instance_name
                """);
    }

    public List<Map<String, Object>> findRelations(String jobId) {
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT r.relation_id, r.job_id, j.job_name,
                           r.related_job_id, rel.job_name AS related_job_name,
                           r.relation_type, r.trigger_condition, r.required_status,
                           r.sort_order, r.use_yn, r.created_at, r.updated_at
                    FROM pfw_batch_job_relation r
                    JOIN pfw_batch_job j ON j.job_id = r.job_id
                    JOIN pfw_batch_job rel ON rel.job_id = r.related_job_id
                    WHERE r.job_id = ?
                       OR r.related_job_id = ?
                    ORDER BY r.job_id, r.sort_order, r.related_job_id
                    """, jobId.trim(), jobId.trim());
        }
        return queryOrEmpty("""
                SELECT r.relation_id, r.job_id, j.job_name,
                       r.related_job_id, rel.job_name AS related_job_name,
                       r.relation_type, r.trigger_condition, r.required_status,
                       r.sort_order, r.use_yn, r.created_at, r.updated_at
                FROM pfw_batch_job_relation r
                JOIN pfw_batch_job j ON j.job_id = r.job_id
                JOIN pfw_batch_job rel ON rel.job_id = r.related_job_id
                ORDER BY r.job_id, r.sort_order, r.related_job_id
                """);
    }

    public List<Map<String, Object>> findExecutionTargets(String jobId, String dispatchStatus, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (TextUtils.hasText(jobId) && TextUtils.hasText(dispatchStatus)) {
            return queryOrEmpty("""
                    SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                           t.target_instance_id, i.instance_name, t.business_date,
                           t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                           t.created_at, t.updated_at
                    FROM pfw_batch_execution_target t
                    JOIN pfw_batch_job j ON j.job_id = t.job_id
                    LEFT JOIN pfw_batch_instance i ON i.instance_id = t.target_instance_id
                    WHERE t.job_id = ?
                      AND t.dispatch_status = ?
                    ORDER BY t.planned_run_at DESC, t.target_id DESC
                    LIMIT ?
                    """, jobId.trim(), dispatchStatus.trim(), resolvedLimit);
        }
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                           t.target_instance_id, i.instance_name, t.business_date,
                           t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                           t.created_at, t.updated_at
                    FROM pfw_batch_execution_target t
                    JOIN pfw_batch_job j ON j.job_id = t.job_id
                    LEFT JOIN pfw_batch_instance i ON i.instance_id = t.target_instance_id
                    WHERE t.job_id = ?
                    ORDER BY t.planned_run_at DESC, t.target_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryOrEmpty("""
                SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                       t.target_instance_id, i.instance_name, t.business_date,
                       t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                       t.created_at, t.updated_at
                FROM pfw_batch_execution_target t
                JOIN pfw_batch_job j ON j.job_id = t.job_id
                LEFT JOIN pfw_batch_instance i ON i.instance_id = t.target_instance_id
                ORDER BY t.planned_run_at DESC, t.target_id DESC
                LIMIT ?
                """, resolvedLimit);
    }

    public List<Map<String, Object>> simulateSchedule(String scheduleId, String baseDate, int days) {
        Map<String, Object> schedule = findSchedule(TextUtils.requireText(scheduleId, "scheduleId"));
        LocalDate startDate = parseDateOrToday(baseDate);
        int resolvedDays = Math.max(1, Math.min(days, 62));
        String calendarId = TextUtils.defaultIfBlank(String.valueOf(schedule.get("calendar_id")), "DEFAULT");
        boolean businessDayOnly = "Y".equalsIgnoreCase(String.valueOf(schedule.get("business_day_only_yn")));
        boolean enabled = "Y".equalsIgnoreCase(String.valueOf(schedule.get("enabled_yn")));
        Map<String, Map<String, Object>> calendarByDate = loadCalendarMap(
                calendarId, startDate, startDate.plusDays(resolvedDays - 1L));

        return startDate.datesUntil(startDate.plusDays(resolvedDays))
                .map(date -> buildSimulationRow(schedule, calendarByDate.get(date.toString()), date, enabled, businessDayOnly))
                .toList();
    }

    public List<Map<String, Object>> findBusinessCalendar(String calendarId, String fromDate, String toDate) {
        String resolvedCalendarId = TextUtils.defaultIfBlank(calendarId, "DEFAULT");
        String from = TextUtils.defaultIfBlank(fromDate, LocalDate.now().withDayOfMonth(1).toString());
        String to = TextUtils.defaultIfBlank(toDate, LocalDate.now().plusMonths(1).toString());
        return queryOrEmpty("""
                SELECT calendar_id, business_date, holiday_yn, business_day_yn, description, created_at, updated_at
                FROM pfw_business_day_calendar
                WHERE calendar_id = ?
                  AND business_date BETWEEN ? AND ?
                ORDER BY business_date
                """, resolvedCalendarId, from, to);
    }

    public Map<String, Object> registerJob(String jobId, String jobName, String jobType, String description, String requestUser) {
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_batch_job (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
                VALUES (?, ?, ?, ?, 'Y', 'Y', ?, ?)
                ON DUPLICATE KEY UPDATE
                    job_name = VALUES(job_name),
                    job_type = VALUES(job_type),
                    description = VALUES(description),
                    use_yn = 'Y',
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                TextUtils.requireText(jobId, "jobId"),
                TextUtils.defaultIfBlank(jobName, jobId),
                TextUtils.defaultIfBlank(jobType, "TASKLET"),
                description,
                user,
                user);
        return findJob(jobId);
    }

    public Map<String, Object> saveBusinessDay(
            String calendarId,
            String businessDate,
            String holidayYn,
            String businessDayYn,
            String description,
            String requestUser) {
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        String resolvedCalendarId = TextUtils.defaultIfBlank(calendarId, "DEFAULT");
        String resolvedDate = TextUtils.requireText(businessDate, "businessDate");
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_business_day_calendar (
                    calendar_id, business_date, holiday_yn, business_day_yn, description, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    holiday_yn = VALUES(holiday_yn),
                    business_day_yn = VALUES(business_day_yn),
                    description = VALUES(description),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                resolvedCalendarId,
                resolvedDate,
                yn(holidayYn, "N"),
                yn(businessDayYn, "Y"),
                description,
                user,
                user);
        return pfwJdbcTemplate.queryForMap("""
                SELECT calendar_id, business_date, holiday_yn, business_day_yn, description, created_at, updated_at
                FROM pfw_business_day_calendar
                WHERE calendar_id = ?
                  AND business_date = ?
                """, resolvedCalendarId, resolvedDate);
    }

    public Map<String, Object> requestRun(String jobId, String jobParameters, String requestUser, String reason) {
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");
        Job job = resolveJob(jobId);
        if (job != null && jobLauncher != null) {
            try {
                JobExecution jobExecution = jobLauncher.run(job, toJobParameters(jobParameters, user));
                long executionId = insertExecution(jobId, null, jobParameters, jobExecution.getStatus().name(), user,
                        jobExecution.getId(), null, null);
                recordOperation(jobId, executionId, "RUN", user, reason, null,
                        "SPRING_BATCH_EXECUTION_ID=" + jobExecution.getId());
                return findExecutionDetail(executionId);
            } catch (Exception ex) {
                long executionId = insertExecution(jobId, null, jobParameters, "FAILED", user, null, null, ex.getMessage());
                recordOperation(jobId, executionId, "RUN_FAILED", user, reason, null, ex.getMessage());
                return findExecutionDetail(executionId);
            }
        }
        long executionId = insertExecution(jobId, null, jobParameters, "REQUESTED", user, null, null, null);
        recordOperation(jobId, executionId, "RUN", user, reason, null, "REQUESTED");
        return findExecution(executionId);
    }

    public Map<String, Object> requestRetry(long executionId, String requestUser, String reason) {
        Map<String, Object> source = findExecution(executionId);
        String jobId = String.valueOf(source.get("job_id"));
        String jobParameters = String.valueOf(source.get("job_parameters"));
        String user = TextUtils.defaultIfBlank(requestUser, "ADM");

        Object springExecutionId = source.get("spring_batch_execution_id");
        if (jobOperator != null && springExecutionId != null) {
            try {
                long restartedId = jobOperator.restart(Long.parseLong(String.valueOf(springExecutionId)));
                long retryExecutionId = insertExecution(jobId, null, jobParameters, "RESTARTED", user, restartedId, null, null);
                recordOperation(jobId, retryExecutionId, "RETRY", user, reason, String.valueOf(source),
                        "SPRING_BATCH_EXECUTION_ID=" + restartedId);
                return findExecutionDetail(retryExecutionId);
            } catch (Exception ex) {
                log.warn("Spring Batch 재시작에 실패했습니다. executionId={}, message={}", executionId, ex.getMessage());
            }
        }

        long retryExecutionId = insertExecution(jobId, null, jobParameters, "REQUESTED", user, null, null, null);
        recordOperation(jobId, retryExecutionId, "RETRY", user, reason, String.valueOf(source), "REQUESTED");
        return findExecution(retryExecutionId);
    }

    public Map<String, Object> requestStop(long executionId, String requestUser, String reason) {
        Map<String, Object> before = findExecution(executionId);
        Object springExecutionId = before.get("spring_batch_execution_id");
        if (jobOperator != null && springExecutionId != null) {
            try {
                jobOperator.stop(Long.parseLong(String.valueOf(springExecutionId)));
            } catch (Exception ex) {
                log.warn("Spring Batch 중지 요청에 실패했습니다. executionId={}, message={}", executionId, ex.getMessage());
            }
        }
        pfwJdbcTemplate.update("""
                UPDATE pfw_batch_execution
                SET execution_status = 'STOPPING',
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                  AND execution_status IN ('STARTING', 'STARTED', 'REQUESTED', 'RESTARTED')
                """, TextUtils.defaultIfBlank(requestUser, "ADM"), executionId);
        Map<String, Object> after = findExecution(executionId);
        recordOperation(String.valueOf(after.get("job_id")), executionId, "STOP",
                TextUtils.defaultIfBlank(requestUser, "ADM"), reason, String.valueOf(before), String.valueOf(after));
        return after;
    }

    public Map<String, Object> updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason) {
        Map<String, Object> before = findSchedule(scheduleId);
        pfwJdbcTemplate.update("""
                UPDATE pfw_batch_schedule
                SET enabled_yn = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE schedule_id = ?
                """, enabled ? "Y" : "N", TextUtils.defaultIfBlank(requestUser, "ADM"), scheduleId);
        Map<String, Object> after = findSchedule(scheduleId);
        recordOperation(String.valueOf(after.get("job_id")), null, enabled ? "SCHEDULE_ENABLE" : "SCHEDULE_DISABLE",
                TextUtils.defaultIfBlank(requestUser, "ADM"), reason, String.valueOf(before), String.valueOf(after));
        return after;
    }

    private long insertExecution(
            String jobId,
            String scheduleId,
            String jobParameters,
            String status,
            String requestUser,
            Long springBatchExecutionId,
            String batchInstanceId,
            String errorMessage) {
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_batch_execution (
                    job_id, schedule_id, job_parameters, execution_status, spring_batch_execution_id,
                    batch_instance_id, error_message, requested_by, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, jobId, scheduleId, TextUtils.defaultIfBlank(jobParameters, "{}"), status,
                springBatchExecutionId, batchInstanceId, errorMessage, requestUser, requestUser, requestUser);
        Long executionId = pfwJdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (executionId == null) {
            throw new IllegalStateException("배치 실행 요청 ID를 확인할 수 없습니다.");
        }
        return executionId;
    }

    private Map<String, Object> findExecution(long executionId) {
        return pfwJdbcTemplate.queryForMap("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, batch_instance_id,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, requested_by, created_at, updated_at
                FROM pfw_batch_execution
                WHERE execution_id = ?
                """, executionId);
    }

    private Map<String, Object> findJob(String jobId) {
        return pfwJdbcTemplate.queryForMap("""
                SELECT job_id, job_name, job_type, description, restartable_yn, use_yn, created_at, updated_at
                FROM pfw_batch_job
                WHERE job_id = ?
                """, jobId);
    }

    private Map<String, Object> findSchedule(String scheduleId) {
        return pfwJdbcTemplate.queryForMap("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM pfw_batch_schedule
                WHERE schedule_id = ?
                """, scheduleId);
    }

    private Map<String, Map<String, Object>> loadCalendarMap(String calendarId, LocalDate from, LocalDate to) {
        List<Map<String, Object>> rows = queryOrEmpty("""
                SELECT calendar_id, business_date, holiday_yn, business_day_yn, description
                FROM pfw_business_day_calendar
                WHERE calendar_id = ?
                  AND business_date BETWEEN ? AND ?
                """, calendarId, from.toString(), to.toString());
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put(String.valueOf(row.get("business_date")), row);
        }
        return result;
    }

    private Map<String, Object> buildSimulationRow(
            Map<String, Object> schedule,
            Map<String, Object> calendar,
            LocalDate date,
            boolean enabled,
            boolean businessDayOnly) {
        String businessDayYn = resolveBusinessDayYn(calendar, date);
        boolean runnable = enabled && (!businessDayOnly || "Y".equals(businessDayYn));
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("schedule_id", schedule.get("schedule_id"));
        row.put("job_id", schedule.get("job_id"));
        row.put("plan_date", date.toString());
        row.put("cron_expression", schedule.get("cron_expression"));
        row.put("timezone", schedule.get("timezone"));
        row.put("calendar_id", schedule.get("calendar_id"));
        row.put("business_day_only_yn", schedule.get("business_day_only_yn"));
        row.put("holiday_policy", schedule.get("holiday_policy"));
        row.put("available_start_time", schedule.get("available_start_time"));
        row.put("available_end_time", schedule.get("available_end_time"));
        row.put("run_date_pattern", schedule.get("run_date_pattern"));
        row.put("business_day_yn", businessDayYn);
        row.put("runnable_yn", runnable ? "Y" : "N");
        row.put("reason", simulationReason(enabled, businessDayOnly, businessDayYn, schedule));
        return row;
    }

    private String resolveBusinessDayYn(Map<String, Object> calendar, LocalDate date) {
        if (calendar != null && calendar.get("business_day_yn") != null) {
            return String.valueOf(calendar.get("business_day_yn"));
        }
        return date.getDayOfWeek().getValue() <= 5 ? "Y" : "N";
    }

    private String simulationReason(
            boolean enabled,
            boolean businessDayOnly,
            String businessDayYn,
            Map<String, Object> schedule) {
        if (!enabled) {
            return "스케줄이 비활성 상태입니다.";
        }
        if (businessDayOnly && !"Y".equals(businessDayYn)) {
            return "영업일 전용 스케줄이며 휴일 정책은 "
                    + TextUtils.defaultIfBlank(String.valueOf(schedule.get("holiday_policy")), "SKIP")
                    + " 입니다.";
        }
        return "수행 가능 후보일입니다.";
    }

    private LocalDate parseDateOrToday(String value) {
        if (!TextUtils.hasText(value)) {
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (DateTimeParseException ex) {
            return LocalDate.now();
        }
    }

    private Object findSpringBatchExecution(Map<String, Object> execution) {
        if (jobExplorer == null || execution.get("spring_batch_execution_id") == null) {
            return null;
        }
        try {
            long springExecutionId = Long.parseLong(String.valueOf(execution.get("spring_batch_execution_id")));
            JobExecution jobExecution = jobExplorer.getJobExecution(springExecutionId);
            return jobExecution == null ? null : Map.of(
                    "id", jobExecution.getId(),
                    "status", jobExecution.getStatus().name(),
                    "exitStatus", jobExecution.getExitStatus().getExitCode(),
                    "startTime", String.valueOf(jobExecution.getStartTime()),
                    "endTime", String.valueOf(jobExecution.getEndTime()));
        } catch (Exception ex) {
            log.debug("Spring Batch 실행 상세를 조회할 수 없습니다. reason={}", ex.getMessage());
            return null;
        }
    }

    private Job resolveJob(String jobId) {
        if (!TextUtils.hasText(jobId) || jobs.isEmpty()) {
            return null;
        }
        Job direct = jobs.get(jobId);
        if (direct != null) {
            return direct;
        }
        return jobs.values().stream()
                .filter(job -> jobId.equals(job.getName()))
                .findFirst()
                .orElse(null);
    }

    private JobParameters toJobParameters(String jobParameters, String requestUser) {
        return new JobParametersBuilder()
                .addString("admJobParameters", TextUtils.defaultIfBlank(jobParameters, "{}"))
                .addString("requestUser", TextUtils.defaultIfBlank(requestUser, "ADM"))
                .addLong("requestTime", System.currentTimeMillis())
                .toJobParameters();
    }

    private List<Map<String, Object>> queryOrEmpty(String sql, Object... args) {
        try {
            return pfwJdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException ex) {
            log.debug("배치 운영 조회를 건너뜁니다. reason={}", ex.getMessage());
            return List.of();
        }
    }

    private void recordOperation(
            String jobId,
            Long executionId,
            String operationType,
            String operatorId,
            String reason,
            String beforeData,
            String afterData) {
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_batch_operation_log (
                    job_id, execution_id, operation_type, operator_id, reason,
                    before_data, after_data, result_type, result_message, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'S', '요청 접수', ?, ?)
                """, jobId, executionId, operationType, operatorId,
                TextUtils.defaultIfBlank(reason, "ADM 배치 운영 요청"), beforeData, afterData, operatorId, operatorId);
    }

    private String yn(String value, String fallback) {
        String normalized = TextUtils.defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }
}
