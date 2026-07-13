package cpf.adm.opr.service;

import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.batch.CpfBatchExecutionRequest;
import cpf.pfw.common.batch.CpfBatchExecutionResult;
import cpf.pfw.common.batch.CpfBatchGhostDetectionService;
import cpf.pfw.common.batch.CpfBatchLauncher;
import cpf.pfw.common.exception.CpfValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
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
 * <p>실행, 재수행, 중지는 PFW 공통 {@link CpfBatchLauncher}를 통해 처리합니다.
 * ADM은 운영자가 보는 조회, 영업일, 관계, 수행 대상, 감사 연결 기능을 담당합니다.</p>
 */
@Service
public class AdmBatchOperationService {
    private static final Logger log = LoggerFactory.getLogger(AdmBatchOperationService.class);

    private final JdbcTemplate pfwJdbcTemplate;
    private final CpfBatchLauncher batchLauncher;
    private final JobExplorer jobExplorer;
    private final CpfBatchGhostDetectionService ghostDetectionService;

    public AdmBatchOperationService(
            @Qualifier("pfwJdbcTemplate") JdbcTemplate pfwJdbcTemplate,
            CpfBatchLauncher batchLauncher,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            ObjectProvider<CpfBatchGhostDetectionService> ghostDetectionServiceProvider) {
        this.pfwJdbcTemplate = pfwJdbcTemplate;
        this.batchLauncher = batchLauncher;
        this.jobExplorer = jobExplorerProvider.getIfAvailable();
        this.ghostDetectionService = ghostDetectionServiceProvider.getIfAvailable();
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

    public Map<String, Object> findJobDetail(String jobId) {
        String resolvedJobId = TextUtils.requireText(jobId, "jobId");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job", findJob(resolvedJobId));
        result.put("schedules", queryOrEmpty("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM pfw_batch_schedule
                WHERE job_id = ?
                ORDER BY schedule_id
                """, resolvedJobId));
        result.put("executions", findExecutions(resolvedJobId, 50));
        result.put("relations", findRelations(resolvedJobId));
        result.put("targets", findExecutionTargets(resolvedJobId, null, 50));
        result.put("locks", findLocks(resolvedJobId));
        return result;
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
                           spring_batch_execution_id, spring_batch_job_instance_id, business_date,
                           run_id, rerun_id, original_job_execution_id, restart_attempt,
                           batch_instance_id, server_instance_id, worker_id,
                           transaction_global_id, parent_transaction_global_id,
                           transaction_segment_id, parent_segment_id, job_log_relative_path,
                           start_time, end_time, read_count, write_count, skip_count,
                           total_count, processed_count, success_count, failure_count, retry_count,
                           progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                           last_heartbeat_at, current_step_name,
                           error_message, requested_by, created_at, updated_at
                    FROM pfw_batch_execution
                    WHERE job_id = ?
                    ORDER BY execution_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryOrEmpty("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, spring_batch_job_instance_id, business_date,
                       run_id, rerun_id, original_job_execution_id, restart_attempt,
                       batch_instance_id, server_instance_id, worker_id,
                       transaction_global_id, parent_transaction_global_id,
                       transaction_segment_id, parent_segment_id, job_log_relative_path,
                       start_time, end_time, read_count, write_count, skip_count,
                       total_count, processed_count, success_count, failure_count, retry_count,
                       progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                       last_heartbeat_at, current_step_name,
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
                    SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id,
                           step_name, execution_status,
                           start_time, end_time, read_count, write_count, skip_count,
                           total_count, processed_count, success_count, failure_count, retry_count,
                           progress_rate, tps, avg_elapsed_ms, max_elapsed_ms, last_heartbeat_at,
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

    public List<Map<String, Object>> findWorkers(int heartbeatTimeoutSeconds) {
        int timeoutSeconds = Math.max(30, Math.min(heartbeatTimeoutSeconds, 86400));
        return queryOrEmpty("""
                SELECT worker_id, server_instance_id, host_name, process_id, thread_name,
                       worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id,
                       CASE
                           WHEN active_yn <> 'Y' THEN 'INACTIVE'
                           WHEN last_heartbeat_at IS NULL THEN 'UNKNOWN'
                           WHEN last_heartbeat_at < TIMESTAMPADD(SECOND, -?, CURRENT_TIMESTAMP(3)) THEN 'STALE'
                           ELSE 'ONLINE'
                       END AS heartbeat_state,
                       created_at, updated_at
                FROM pfw_batch_worker
                ORDER BY active_yn DESC, last_heartbeat_at DESC, worker_id
                """, timeoutSeconds);
    }

    public List<Map<String, Object>> findStepExecutions(Long executionId, String jobId, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (executionId != null) {
            return queryOrEmpty("""
                    SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                           s.worker_id, s.step_name, s.execution_status,
                           s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                           s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                           s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                           s.error_message, s.step_log, s.created_at, s.updated_at
                    FROM pfw_batch_step_execution s
                    WHERE s.execution_id = ?
                    ORDER BY s.step_execution_id
                    LIMIT ?
                    """, executionId, resolvedLimit);
        }
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                           s.worker_id, s.step_name, s.execution_status,
                           s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                           s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                           s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                           s.error_message, s.step_log, s.created_at, s.updated_at
                    FROM pfw_batch_step_execution s
                    JOIN pfw_batch_execution e ON e.execution_id = s.execution_id
                    WHERE e.job_id = ?
                    ORDER BY s.step_execution_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryOrEmpty("""
                SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                       s.worker_id, s.step_name, s.execution_status,
                       s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                       s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                       s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                       s.error_message, s.step_log, s.created_at, s.updated_at
                FROM pfw_batch_step_execution s
                ORDER BY s.step_execution_id DESC
                LIMIT ?
                """, resolvedLimit);
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

    public List<Map<String, Object>> findLocks(String jobId) {
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                           CASE WHEN expire_at <= CURRENT_TIMESTAMP(3) THEN 'EXPIRED' ELSE 'ACTIVE' END AS lock_state,
                           created_at, updated_at
                    FROM pfw_batch_lock
                    WHERE job_id = ?
                    ORDER BY locked_at DESC
                    """, jobId.trim());
        }
        return queryOrEmpty("""
                SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                       CASE WHEN expire_at <= CURRENT_TIMESTAMP(3) THEN 'EXPIRED' ELSE 'ACTIVE' END AS lock_state,
                       created_at, updated_at
                FROM pfw_batch_lock
                ORDER BY locked_at DESC
                """);
    }

    public Map<String, Object> releaseLock(String lockKey, String requestUser, String reason) {
        String resolvedLockKey = TextUtils.requireText(lockKey, "lockKey");
        Map<String, Object> before = findLock(resolvedLockKey);
        int deleted = pfwJdbcTemplate.update("DELETE FROM pfw_batch_lock WHERE lock_key = ?", resolvedLockKey);
        String operatorId = TextUtils.defaultIfBlank(requestUser, "ADM");
        recordOperation(String.valueOf(before.get("job_id")), null, "LOCK_RELEASE", operatorId, reason,
                String.valueOf(before), "deleted=" + deleted);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lockKey", resolvedLockKey);
        result.put("released", deleted > 0);
        result.put("before", before);
        return result;
    }

    public List<Map<String, Object>> findGhostCandidates(int heartbeatTimeoutSeconds) {
        int timeoutSeconds = Math.max(30, Math.min(heartbeatTimeoutSeconds, 86400));
        if (ghostDetectionService != null) {
            ghostDetectionService.detectGhostCandidates(timeoutSeconds);
        }
        return queryOrEmpty("""
                SELECT e.execution_id, e.job_id, j.job_name, e.schedule_id, e.job_parameters,
                       e.execution_status, e.spring_batch_execution_id, e.batch_instance_id,
                       e.server_instance_id, e.worker_id, e.transaction_global_id,
                       e.start_time, e.end_time, e.last_heartbeat_at AS execution_last_heartbeat_at,
                       e.current_step_name, e.progress_rate, e.processed_count, e.total_count, e.requested_by,
                       g.ghost_event_id, g.detected_at AS ghost_detected_at, g.detected_reason AS ghost_detected_reason,
                       w.worker_status, w.last_heartbeat_at,
                       CASE
                           WHEN w.worker_id IS NULL THEN '실행 worker heartbeat가 없습니다.'
                           WHEN w.last_heartbeat_at IS NULL THEN 'worker heartbeat 시각이 없습니다.'
                           WHEN w.last_heartbeat_at < TIMESTAMPADD(SECOND, -?, CURRENT_TIMESTAMP(3)) THEN 'worker heartbeat 제한 시간을 초과했습니다.'
                           ELSE '실행 중 상태가 장시간 종료되지 않았습니다.'
                       END AS detected_reason
                FROM pfw_batch_execution e
                JOIN pfw_batch_job j ON j.job_id = e.job_id
                LEFT JOIN pfw_batch_worker w ON w.worker_id = e.worker_id
                LEFT JOIN pfw_batch_ghost_event g
                       ON g.execution_id = e.execution_id
                      AND g.ghost_status = 'DETECTED'
                WHERE e.end_time IS NULL
                  AND e.execution_status IN ('REQUESTED', 'STARTING', 'STARTED', 'RUNNING', 'UNKNOWN', 'STOPPING')
                  AND (
                      w.worker_id IS NULL
                      OR w.last_heartbeat_at IS NULL
                      OR w.last_heartbeat_at < TIMESTAMPADD(SECOND, -?, CURRENT_TIMESTAMP(3))
                  )
                ORDER BY e.start_time, e.execution_id
                """, timeoutSeconds, timeoutSeconds);
    }

    public Map<String, Object> actGhostExecution(long executionId, String actionType, String requestUser, String reason) {
        String action = normalizeGhostAction(actionType);
        String operatorId = TextUtils.defaultIfBlank(requestUser, "ADM");
        Map<String, Object> before = findExecution(executionId);
        String jobId = String.valueOf(before.get("job_id"));
        int releasedLocks = 0;
        if ("FAIL".equals(action)) {
            pfwJdbcTemplate.update("""
                    UPDATE pfw_batch_execution
                    SET execution_status = 'FAILED',
                        end_time = COALESCE(end_time, CURRENT_TIMESTAMP(3)),
                        error_message = COALESCE(error_message, 'ADM ghost 조치로 실패 처리되었습니다.'),
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                    """, operatorId, executionId);
            releasedLocks = releaseLocksForExecution(before);
        } else if ("ABANDON".equals(action)) {
            pfwJdbcTemplate.update("""
                    UPDATE pfw_batch_execution
                    SET execution_status = 'ABANDONED',
                        end_time = COALESCE(end_time, CURRENT_TIMESTAMP(3)),
                        error_message = COALESCE(error_message, 'ADM ghost 조치로 폐기 처리되었습니다.'),
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                    """, operatorId, executionId);
            releasedLocks = releaseLocksForExecution(before);
        } else if ("RELEASE_LOCK".equals(action)) {
            releasedLocks = releaseLocksForExecution(before);
        }
        Map<String, Object> after = findExecution(executionId);
        pfwJdbcTemplate.update("""
                INSERT INTO pfw_batch_ghost_event (
                    execution_id, spring_batch_execution_id, job_id, server_instance_id, worker_id,
                    ghost_status, detected_reason, action_type, action_reason, action_by, action_at,
                    lock_released_yn, retryable_yn, before_data, after_data, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, 'ACTIONED', ?, ?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?, ?, ?, ?, ?)
                """,
                executionId,
                before.get("spring_batch_execution_id"),
                jobId,
                before.get("server_instance_id"),
                before.get("worker_id"),
                "ADM에서 ghost 후보를 조치했습니다. action=" + action,
                action,
                TextUtils.requireText(reason, "reason"),
                operatorId,
                releasedLocks > 0 ? "Y" : "N",
                "RELEASE_LOCK".equals(action) ? "Y" : "N",
                String.valueOf(before),
                String.valueOf(after),
                operatorId,
                operatorId);
        recordOperation(jobId, executionId, "GHOST_" + action, operatorId, reason,
                String.valueOf(before), String.valueOf(after) + ", releasedLocks=" + releasedLocks);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("execution", after);
        result.put("actionType", action);
        result.put("releasedLocks", releasedLocks);
        return result;
    }

    public List<Map<String, Object>> findOperationLogs(String jobId, Long executionId, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (executionId != null) {
            return queryOrEmpty("""
                    SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                           reason, before_data, after_data, result_type, result_message,
                           created_at, updated_at
                    FROM pfw_batch_operation_log
                    WHERE execution_id = ?
                    ORDER BY operation_id DESC
                    LIMIT ?
                    """, executionId, resolvedLimit);
        }
        if (TextUtils.hasText(jobId)) {
            return queryOrEmpty("""
                    SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                           reason, before_data, after_data, result_type, result_message,
                           created_at, updated_at
                    FROM pfw_batch_operation_log
                    WHERE job_id = ?
                    ORDER BY operation_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryOrEmpty("""
                SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                       reason, before_data, after_data, result_type, result_message,
                       created_at, updated_at
                FROM pfw_batch_operation_log
                ORDER BY operation_id DESC
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
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.run(
                jobId, jobParameters, requestUser, reason));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestScheduledRun(
            String scheduleId,
            String jobId,
            String jobParameters,
            String requestUser,
            String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.scheduledRun(
                scheduleId, jobId, jobParameters, requestUser, reason));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestRetry(long executionId, String requestUser, String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.retry(
                executionId, requestUser, reason));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestStop(long executionId, String requestUser, String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.stop(
                executionId, requestUser, reason));
        return toAdmExecutionResult(result);
    }

    private Map<String, Object> toAdmExecutionResult(CpfBatchExecutionResult result) {
        if (result.pfwExecutionId() != null && result.pfwExecutionId() > 0) {
            return findExecutionDetail(result.pfwExecutionId());
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("executed", result.executed());
        response.put("jobId", result.jobId());
        response.put("executionId", result.pfwExecutionId());
        response.put("springBatchExecutionId", result.springBatchExecutionId());
        response.put("status", result.status());
        response.put("message", result.message());
        response.put("detail", result.detail());
        return response;
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

    private Map<String, Object> findExecution(long executionId) {
        return pfwJdbcTemplate.queryForMap("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, spring_batch_job_instance_id, business_date,
                       run_id, rerun_id, original_job_execution_id, restart_attempt,
                       batch_instance_id, server_instance_id, worker_id,
                       transaction_global_id, parent_transaction_global_id,
                       transaction_segment_id, parent_segment_id, job_log_relative_path,
                       start_time, end_time, read_count, write_count, skip_count,
                       total_count, processed_count, success_count, failure_count, retry_count,
                       progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                       last_heartbeat_at, current_step_name,
                       error_message, requested_by, created_at, updated_at
                FROM pfw_batch_execution
                WHERE execution_id = ?
                """, executionId);
    }

    private Map<String, Object> findLock(String lockKey) {
        try {
            return pfwJdbcTemplate.queryForMap("""
                    SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                           created_at, updated_at
                    FROM pfw_batch_lock
                    WHERE lock_key = ?
                    """, lockKey);
        } catch (DataAccessException ex) {
            throw new CpfValidationException("해제할 배치 lock을 찾을 수 없습니다. lockKey=" + lockKey);
        }
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

    private int releaseLocksForExecution(Map<String, Object> execution) {
        String jobId = String.valueOf(execution.get("job_id"));
        Object workerId = execution.get("worker_id");
        Object serverInstanceId = execution.get("server_instance_id");
        Object batchInstanceId = execution.get("batch_instance_id");
        return pfwJdbcTemplate.update("""
                DELETE FROM pfw_batch_lock
                WHERE job_id = ?
                  AND (
                      owner_id = ?
                      OR owner_id = ?
                      OR owner_id = ?
                      OR ? IS NULL
                  )
                """,
                jobId,
                workerId,
                serverInstanceId,
                batchInstanceId,
                workerId == null && serverInstanceId == null && batchInstanceId == null ? null : "HAS_OWNER");
    }

    private String normalizeGhostAction(String actionType) {
        String action = TextUtils.defaultIfBlank(actionType, "FAIL").trim().toUpperCase();
        if ("FAIL".equals(action) || "ABANDON".equals(action) || "RELEASE_LOCK".equals(action)) {
            return action;
        }
        throw new CpfValidationException("지원하지 않는 배치 ghost 조치 유형입니다. actionType=" + actionType);
    }

    private String yn(String value, String fallback) {
        String normalized = TextUtils.defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }
}
