package com.cpf.batch.operation;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.common.calendar.CmnBusinessCalendar;
import com.cpf.batch.scheduler.BatBatchScheduler;
import com.cpf.core.api.batch.CpfBatchExecutionRequest;
import com.cpf.core.api.batch.CpfBatchExecutionResult;
import com.cpf.batch.runtime.BatBatchGhostDetectionService;
import com.cpf.batch.runtime.BatBatchLauncher;
import com.cpf.core.api.error.CpfValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cpf.core.api.logging.CpfTransactionContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * CPF 배치 운영 메타와 Spring Batch 실행 기능을 연결합니다.
 *
 * <p>실행, 재수행, 중지는 CPF 공통 {@link BatBatchLauncher}를 통해 처리합니다.
 * ADM은 운영자가 보는 조회, 영업일, 관계, 수행 대상, 감사 연결 기능을 담당합니다.</p>
 */
@Service
public class BatOperationFacade implements CpfBatchOperationsPort {
    private static final Logger log = LoggerFactory.getLogger(BatOperationFacade.class);

    private final JdbcTemplate batJdbcTemplate;
    private final BatBatchLauncher batchLauncher;
    private final JobExplorer jobExplorer;
    private final BatBatchGhostDetectionService ghostDetectionService;
    private final org.springframework.beans.factory.ObjectProvider<BatBatchScheduler> schedulerProvider;
    private final CmnBusinessCalendar businessCalendar;

    public BatOperationFacade(
            @Qualifier("batJdbcTemplate") JdbcTemplate batJdbcTemplate,
            BatBatchLauncher batchLauncher,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            ObjectProvider<BatBatchGhostDetectionService> ghostDetectionServiceProvider,
            org.springframework.beans.factory.ObjectProvider<BatBatchScheduler> schedulerProvider,
            CmnBusinessCalendar businessCalendar) {
        this.batJdbcTemplate = batJdbcTemplate;
        this.batchLauncher = batchLauncher;
        this.jobExplorer = jobExplorerProvider.getIfAvailable();
        this.ghostDetectionService = ghostDetectionServiceProvider.getIfAvailable();
        this.schedulerProvider = schedulerProvider;
        this.businessCalendar = businessCalendar;
    }

    public List<Map<String, Object>> findJobs() {
        return queryRequired("""
                SELECT j.job_id, j.job_name, j.job_type, j.description, j.restartable_yn, j.use_yn,
                       MAX(e.start_time) AS last_start_time,
                       MAX(e.end_time) AS last_end_time,
                       SUM(CASE WHEN e.execution_status = 'COMPLETED' THEN 1 ELSE 0 END) AS success_count,
                       SUM(CASE WHEN e.execution_status IN ('FAILED', 'STOPPED') THEN 1 ELSE 0 END) AS failure_count,
                       AVG(TIMESTAMPDIFF(SECOND, e.start_time, e.end_time)) AS avg_duration_seconds
                FROM bat_job j
                LEFT JOIN bat_execution e ON e.job_id = j.job_id
                WHERE j.use_yn = 'Y'
                GROUP BY j.job_id, j.job_name, j.job_type, j.description, j.restartable_yn, j.use_yn
                ORDER BY j.job_id
                """);
    }

    public Map<String, Object> findJobDetail(String jobId) {
        String resolvedJobId = requireText(jobId, "jobId");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("job", findJob(resolvedJobId));
        result.put("schedules", queryRequired("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM bat_schedule
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
        return queryRequired("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM bat_schedule
                ORDER BY job_id, schedule_id
                """);
    }

    @Override
    public List<Map<String, Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            int limit) {
        return findExecutions(jobId, transactionId, springBatchJobInstanceId, workerId, serverInstanceId, null, null, limit);
    }

    @Override
    public List<Map<String, Object>> findExecutions(
            String jobId,
            String transactionId,
            Long springBatchJobInstanceId,
            String workerId,
            String serverInstanceId,
            String fromDate,
            String toDate,
            int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 10000));
        StringBuilder sql = new StringBuilder("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, spring_batch_job_instance_id, business_date,
                       run_id, rerun_id, original_job_execution_id, restart_attempt,
                       batch_instance_id, server_instance_id, worker_id,
                       transaction_id, transaction_segment_id, parent_segment_id, job_log_relative_path,
                       start_time, end_time, read_count, write_count, skip_count,
                       total_count, processed_count, success_count, failure_count, retry_count,
                       progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                       last_heartbeat_at, current_step_name,
                       error_message, requested_by, created_at, updated_at
                  FROM bat_execution
                 WHERE 1=1
                """);
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        appendEquals(sql, args, "job_id", jobId);
        appendEquals(sql, args, "transaction_id", transactionId);
        if (springBatchJobInstanceId != null) {
            sql.append(" AND spring_batch_job_instance_id = ?");
            args.add(springBatchJobInstanceId);
        }
        appendEquals(sql, args, "worker_id", workerId);
        appendEquals(sql, args, "server_instance_id", serverInstanceId);
        if (fromDate != null && !fromDate.isBlank()) {
            sql.append(" AND created_at >= ?");
            args.add(fromDate.trim());
        }
        if (toDate != null && !toDate.isBlank()) {
            sql.append(" AND created_at <= ?");
            args.add(toDate.trim());
        }
        sql.append(" ORDER BY execution_id DESC LIMIT ?");
        args.add(resolvedLimit);
        return queryRequired(sql.toString(), args.toArray());
    }

    public Map<String, Object> findExecutionDetail(long executionId) {
        try {
            Map<String, Object> execution = findExecution(executionId);
            List<Map<String, Object>> steps = batJdbcTemplate.queryForList("""
                    SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id,
                           step_name, execution_status,
                           start_time, end_time, read_count, write_count, skip_count,
                           total_count, processed_count, success_count, failure_count, retry_count,
                           progress_rate, tps, avg_elapsed_ms, max_elapsed_ms, last_heartbeat_at,
                           error_message, step_log, created_at, updated_at
                    FROM bat_step_execution
                    WHERE execution_id = ?
                    ORDER BY step_execution_id
                    """, executionId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("execution", execution);
            result.put("steps", steps);
            result.put("springBatch", findSpringBatchExecution(execution));
            return result;
        } catch (DataAccessException ex) {
            throw queryFailure("executionDetail", ex);
        }
    }

    public List<Map<String, Object>> findInstances() {
        return queryRequired("""
                SELECT instance_id, instance_name, host_name, server_port, active_yn,
                       last_heartbeat_at, description, created_at, updated_at
                FROM bat_instance
                ORDER BY active_yn DESC, instance_name
                """);
    }

    public List<Map<String, Object>> findWorkers(int heartbeatTimeoutSeconds) {
        int timeoutSeconds = Math.max(30, Math.min(heartbeatTimeoutSeconds, 86400));
        return queryRequired("""
                SELECT worker_id, server_instance_id, host_name, process_id, thread_name,
                       worker_status, active_yn, last_heartbeat_at, current_job_id, current_execution_id,
                       CASE
                           WHEN active_yn <> 'Y' THEN 'INACTIVE'
                           WHEN last_heartbeat_at IS NULL THEN 'UNKNOWN'
                           WHEN last_heartbeat_at < TIMESTAMPADD(SECOND, -?, CURRENT_TIMESTAMP(3)) THEN 'STALE'
                           ELSE 'ONLINE'
                       END AS heartbeat_state,
                       created_at, updated_at
                FROM bat_worker
                ORDER BY active_yn DESC, last_heartbeat_at DESC, worker_id
                """, timeoutSeconds);
    }

    public List<Map<String, Object>> findStepExecutions(Long executionId, String jobId, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (executionId != null) {
            return queryRequired("""
                    SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                           s.worker_id, s.step_name, s.execution_status,
                           s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                           s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                           s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                           s.error_message, s.step_log, s.created_at, s.updated_at
                    FROM bat_step_execution s
                    WHERE s.execution_id = ?
                    ORDER BY s.step_execution_id
                    LIMIT ?
                    """, executionId, resolvedLimit);
        }
        if (hasText(jobId)) {
            return queryRequired("""
                    SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                           s.worker_id, s.step_name, s.execution_status,
                           s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                           s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                           s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                           s.error_message, s.step_log, s.created_at, s.updated_at
                    FROM bat_step_execution s
                    JOIN bat_execution e ON e.execution_id = s.execution_id
                    WHERE e.job_id = ?
                    ORDER BY s.step_execution_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryRequired("""
                SELECT s.step_execution_id, s.execution_id, s.spring_batch_step_execution_id,
                       s.worker_id, s.step_name, s.execution_status,
                       s.start_time, s.end_time, s.read_count, s.write_count, s.skip_count,
                       s.total_count, s.processed_count, s.success_count, s.failure_count, s.retry_count,
                       s.progress_rate, s.tps, s.avg_elapsed_ms, s.max_elapsed_ms, s.last_heartbeat_at,
                       s.error_message, s.step_log, s.created_at, s.updated_at
                FROM bat_step_execution s
                ORDER BY s.step_execution_id DESC
                LIMIT ?
                """, resolvedLimit);
    }

    public List<Map<String, Object>> findRelations(String jobId) {
        if (hasText(jobId)) {
            return queryRequired("""
                    SELECT r.relation_id, r.job_id, j.job_name,
                           r.related_job_id, rel.job_name AS related_job_name,
                           r.relation_type, r.trigger_condition, r.required_status,
                           r.sort_order, r.use_yn, r.created_at, r.updated_at
                    FROM bat_job_relation r
                    JOIN bat_job j ON j.job_id = r.job_id
                    JOIN bat_job rel ON rel.job_id = r.related_job_id
                    WHERE r.job_id = ?
                       OR r.related_job_id = ?
                    ORDER BY r.job_id, r.sort_order, r.related_job_id
                    """, jobId.trim(), jobId.trim());
        }
        return queryRequired("""
                SELECT r.relation_id, r.job_id, j.job_name,
                       r.related_job_id, rel.job_name AS related_job_name,
                       r.relation_type, r.trigger_condition, r.required_status,
                       r.sort_order, r.use_yn, r.created_at, r.updated_at
                FROM bat_job_relation r
                JOIN bat_job j ON j.job_id = r.job_id
                JOIN bat_job rel ON rel.job_id = r.related_job_id
                ORDER BY r.job_id, r.sort_order, r.related_job_id
                """);
    }

    public List<Map<String, Object>> findExecutionTargets(String jobId, String dispatchStatus, int limit) {
        int resolvedLimit = Math.max(1, Math.min(limit, 500));
        if (hasText(jobId) && hasText(dispatchStatus)) {
            return queryRequired("""
                    SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                           t.target_instance_id, i.instance_name, t.business_date,
                           t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                           t.created_at, t.updated_at
                    FROM bat_execution_target t
                    JOIN bat_job j ON j.job_id = t.job_id
                    LEFT JOIN bat_instance i ON i.instance_id = t.target_instance_id
                    WHERE t.job_id = ?
                      AND t.dispatch_status = ?
                    ORDER BY t.planned_run_at DESC, t.target_id DESC
                    LIMIT ?
                    """, jobId.trim(), dispatchStatus.trim(), resolvedLimit);
        }
        if (hasText(jobId)) {
            return queryRequired("""
                    SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                           t.target_instance_id, i.instance_name, t.business_date,
                           t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                           t.created_at, t.updated_at
                    FROM bat_execution_target t
                    JOIN bat_job j ON j.job_id = t.job_id
                    LEFT JOIN bat_instance i ON i.instance_id = t.target_instance_id
                    WHERE t.job_id = ?
                    ORDER BY t.planned_run_at DESC, t.target_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryRequired("""
                SELECT t.target_id, t.execution_id, t.job_id, j.job_name, t.schedule_id,
                       t.target_instance_id, i.instance_name, t.business_date,
                       t.planned_run_at, t.dispatch_status, t.dispatch_reason,
                       t.created_at, t.updated_at
                FROM bat_execution_target t
                JOIN bat_job j ON j.job_id = t.job_id
                LEFT JOIN bat_instance i ON i.instance_id = t.target_instance_id
                ORDER BY t.planned_run_at DESC, t.target_id DESC
                LIMIT ?
                """, resolvedLimit);
    }

    public List<Map<String, Object>> findLocks(String jobId) {
        if (hasText(jobId)) {
            return queryRequired("""
                    SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                           CASE WHEN expire_at <= CURRENT_TIMESTAMP(3) THEN 'EXPIRED' ELSE 'ACTIVE' END AS lock_state,
                           created_at, updated_at
                    FROM bat_lock
                    WHERE job_id = ?
                    ORDER BY locked_at DESC
                    """, jobId.trim());
        }
        return queryRequired("""
                SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                       CASE WHEN expire_at <= CURRENT_TIMESTAMP(3) THEN 'EXPIRED' ELSE 'ACTIVE' END AS lock_state,
                       created_at, updated_at
                FROM bat_lock
                ORDER BY locked_at DESC
                """);
    }

    @Transactional(transactionManager = "batTransactionManager")
    public Map<String, Object> releaseLock(String lockKey, String requestUser, String reason) {
        String resolvedLockKey = requireText(lockKey, "lockKey");
        String operatorId = requireText(requestUser, "requestUser");
        String resolvedReason = requireText(reason, "reason");
        Map<String, Object> before = findLock(resolvedLockKey);
        String ownerId = requireObjectText(before.get("owner_id"), "lock.ownerId");
        int deleted = batJdbcTemplate.update(
                "DELETE FROM bat_lock WHERE lock_key = ? AND owner_id = ?",
                resolvedLockKey, ownerId);
        if (deleted != 1) {
            throw new CpfValidationException("배치 lock 소유권이 변경되어 해제하지 않았습니다. lockKey=" + resolvedLockKey);
        }
        recordOperation(String.valueOf(before.get("job_id")), null, "LOCK_RELEASE", operatorId, resolvedReason,
                String.valueOf(before), "deleted=" + deleted + ", ownerId=" + ownerId);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("lockKey", resolvedLockKey);
        result.put("released", true);
        result.put("ownerId", ownerId);
        result.put("before", before);
        return result;
    }

    public List<Map<String, Object>> findGhostCandidates(int heartbeatTimeoutSeconds) {
        int timeoutSeconds = Math.max(30, Math.min(heartbeatTimeoutSeconds, 86400));
        if (ghostDetectionService != null) {
            ghostDetectionService.detectGhostCandidates(timeoutSeconds);
        }
        return queryRequired("""
                SELECT e.execution_id, e.job_id, j.job_name, e.schedule_id, e.job_parameters,
                       e.execution_status, e.spring_batch_execution_id, e.batch_instance_id,
                       e.server_instance_id, e.worker_id, e.transaction_id,
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
                FROM bat_execution e
                JOIN bat_job j ON j.job_id = e.job_id
                LEFT JOIN bat_worker w ON w.worker_id = e.worker_id
                LEFT JOIN bat_ghost_event g
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

    @Transactional(transactionManager = "batTransactionManager")
    public Map<String, Object> actGhostExecution(long executionId, String actionType, String requestUser, String reason) {
        String action = normalizeGhostAction(actionType);
        String operatorId = requireText(requestUser, "requestUser");
        String resolvedReason = requireText(reason, "reason");
        Map<String, Object> before = findExecution(executionId);
        String jobId = requireObjectText(before.get("job_id"), "execution.jobId");
        int releasedLocks;
        if ("FAIL".equals(action) || "ABANDON".equals(action)) {
            String targetStatus = "FAIL".equals(action) ? "FAILED" : "ABANDONED";
            String message = "FAIL".equals(action)
                    ? "ADM ghost 조치로 실패 처리되었습니다."
                    : "ADM ghost 조치로 폐기 처리되었습니다.";
            int updated = batJdbcTemplate.update("""
                    UPDATE bat_execution
                    SET execution_status = ?,
                        end_time = CURRENT_TIMESTAMP(3),
                        error_message = COALESCE(error_message, ?),
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                      AND end_time IS NULL
                      AND execution_status IN ('REQUESTED','STARTING','STARTED','RUNNING','UNKNOWN','STOPPING')
                    """, targetStatus, message, operatorId, executionId);
            if (updated != 1) {
                throw new CpfValidationException(
                        "배치 실행 상태가 이미 변경되었거나 종료되어 ghost 조치를 적용하지 않았습니다. executionId=" + executionId);
            }
            releasedLocks = releaseLocksForExecution(before);
        } else {
            releasedLocks = releaseLocksForExecution(before);
        }
        Map<String, Object> after = findExecution(executionId);
        batJdbcTemplate.update("""
                INSERT INTO bat_ghost_event (
                    execution_id, spring_batch_execution_id, job_id, server_instance_id, worker_id,
                    ghost_status, detected_reason, action_type, action_reason, action_by, action_at,
                    lock_released_yn, retryable_yn, before_data, after_data, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, 'ACTIONED', ?, ?, ?, ?, CURRENT_TIMESTAMP(3), ?, ?, ?, ?, ?, ?)
                """,
                executionId, before.get("spring_batch_execution_id"), jobId,
                before.get("server_instance_id"), before.get("worker_id"),
                "ADM에서 ghost 후보를 조치했습니다. action=" + action, action, resolvedReason, operatorId,
                releasedLocks > 0 ? "Y" : "N", "RELEASE_LOCK".equals(action) ? "Y" : "N",
                String.valueOf(before), String.valueOf(after), operatorId, operatorId);
        recordOperation(jobId, executionId, "GHOST_" + action, operatorId, resolvedReason,
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
            return queryRequired("""
                    SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                           reason, before_data, after_data, result_type, result_message,
                           created_at, updated_at
                    FROM bat_operation_log
                    WHERE execution_id = ?
                    ORDER BY operation_id DESC
                    LIMIT ?
                    """, executionId, resolvedLimit);
        }
        if (hasText(jobId)) {
            return queryRequired("""
                    SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                           reason, before_data, after_data, result_type, result_message,
                           created_at, updated_at
                    FROM bat_operation_log
                    WHERE job_id = ?
                    ORDER BY operation_id DESC
                    LIMIT ?
                    """, jobId.trim(), resolvedLimit);
        }
        return queryRequired("""
                SELECT operation_id, job_id, execution_id, operation_type, operator_id,
                       reason, before_data, after_data, result_type, result_message,
                       created_at, updated_at
                FROM bat_operation_log
                ORDER BY operation_id DESC
                LIMIT ?
                """, resolvedLimit);
    }

    public List<Map<String, Object>> simulateSchedule(String scheduleId, String baseDate, int days) {
        Map<String, Object> schedule = findSchedule(requireText(scheduleId, "scheduleId"));
        LocalDate startDate = parseDateOrToday(baseDate);
        int resolvedDays = Math.max(1, Math.min(days, 62));
        String calendarId = defaultIfBlank(String.valueOf(schedule.get("calendar_id")), "DEFAULT");
        boolean businessDayOnly = "Y".equalsIgnoreCase(String.valueOf(schedule.get("business_day_only_yn")));
        boolean enabled = "Y".equalsIgnoreCase(String.valueOf(schedule.get("enabled_yn")));
        return startDate.datesUntil(startDate.plusDays(resolvedDays))
                .map(date -> buildSimulationRow(schedule, calendarId, date, enabled, businessDayOnly))
                .toList();
    }


    public Map<String, Object> registerJob(String jobId, String jobName, String jobType, String description, String requestUser) {
        String user = requireText(requestUser, "requestUser");
        batJdbcTemplate.update("""
                INSERT INTO bat_job (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
                VALUES (?, ?, ?, ?, 'Y', 'Y', ?, ?)
                ON DUPLICATE KEY UPDATE
                    job_name = VALUES(job_name),
                    job_type = VALUES(job_type),
                    description = VALUES(description),
                    use_yn = 'Y',
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                requireText(jobId, "jobId"),
                defaultIfBlank(jobName, jobId),
                defaultIfBlank(jobType, "TASKLET"),
                description,
                user,
                user);
        return findJob(jobId);
    }


    public Map<String, Object> requestRun(String jobId, String jobParameters, String requestUser, String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.run(
                jobId, jobParameters, requireText(requestUser, "requestUser"), requireText(reason, "reason")));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestScheduledRun(
            String scheduleId,
            String jobId,
            String jobParameters,
            String requestUser,
            String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.scheduledRun(
                scheduleId, jobId, jobParameters, requireText(requestUser, "requestUser"), requireText(reason, "reason")));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestRetry(long executionId, String requestUser, String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.retry(
                executionId, requireText(requestUser, "requestUser"), requireText(reason, "reason")));
        return toAdmExecutionResult(result);
    }

    public Map<String, Object> requestStop(long executionId, String requestUser, String reason) {
        CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.stop(
                executionId, requireText(requestUser, "requestUser"), requireText(reason, "reason")));
        return toAdmExecutionResult(result);
    }

    private Map<String, Object> toAdmExecutionResult(CpfBatchExecutionResult result) {
        if (result.cpfExecutionId() != null && result.cpfExecutionId() > 0) {
            return findExecutionDetail(result.cpfExecutionId());
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("executed", result.executed());
        response.put("jobId", result.jobId());
        response.put("executionId", result.cpfExecutionId());
        response.put("springBatchExecutionId", result.springBatchExecutionId());
        response.put("status", result.status());
        response.put("message", result.message());
        response.put("detail", result.detail());
        return response;
    }

    public Map<String, Object> updateScheduleEnabled(String scheduleId, boolean enabled, String requestUser, String reason) {
        Map<String, Object> before = findSchedule(scheduleId);
        batJdbcTemplate.update("""
                UPDATE bat_schedule
                SET enabled_yn = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE schedule_id = ?
                """, enabled ? "Y" : "N", requireText(requestUser, "requestUser"), scheduleId);
        Map<String, Object> after = findSchedule(scheduleId);
        recordOperation(String.valueOf(after.get("job_id")), null, enabled ? "SCHEDULE_ENABLE" : "SCHEDULE_DISABLE",
                requireText(requestUser, "requestUser"), reason, String.valueOf(before), String.valueOf(after));
        return after;
    }

    private Map<String, Object> findExecution(long executionId) {
        return batJdbcTemplate.queryForMap("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, spring_batch_job_instance_id, business_date,
                       run_id, rerun_id, original_job_execution_id, restart_attempt,
                       batch_instance_id, server_instance_id, worker_id,
                       transaction_id, transaction_segment_id, parent_segment_id, job_log_relative_path,
                       start_time, end_time, read_count, write_count, skip_count,
                       total_count, processed_count, success_count, failure_count, retry_count,
                       progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                       last_heartbeat_at, current_step_name,
                       error_message, requested_by, created_at, updated_at
                FROM bat_execution
                WHERE execution_id = ?
                """, executionId);
    }

    private Map<String, Object> findLock(String lockKey) {
        try {
            return batJdbcTemplate.queryForMap("""
                    SELECT lock_key, job_id, job_parameters_hash, owner_id, locked_at, expire_at,
                           created_at, updated_at
                    FROM bat_lock
                    WHERE lock_key = ?
                    """, lockKey);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            throw new CpfValidationException("해제할 배치 lock을 찾을 수 없습니다. lockKey=" + lockKey);
        } catch (DataAccessException ex) {
            throw queryFailure("findLock", ex);
        }
    }

    private Map<String, Object> findJob(String jobId) {
        return batJdbcTemplate.queryForMap("""
                SELECT job_id, job_name, job_type, description, restartable_yn, use_yn, created_at, updated_at
                FROM bat_job
                WHERE job_id = ?
                """, jobId);
    }

    private Map<String, Object> findSchedule(String scheduleId) {
        return batJdbcTemplate.queryForMap("""
                SELECT schedule_id, job_id, cron_expression, timezone, enabled_yn,
                       calendar_id, business_day_only_yn, holiday_policy,
                       available_start_time, available_end_time, run_date_pattern,
                       last_fire_at, next_fire_at, created_at, updated_at
                FROM bat_schedule
                WHERE schedule_id = ?
                """, scheduleId);
    }

    private Map<String, Object> buildSimulationRow(
            Map<String, Object> schedule,
            String calendarId,
            LocalDate date,
            boolean enabled,
            boolean businessDayOnly) {
        String businessDayYn = businessCalendar.isBusinessDay(calendarId, date) ? "Y" : "N";
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
                    + defaultIfBlank(String.valueOf(schedule.get("holiday_policy")), "SKIP")
                    + " 입니다.";
        }
        return "수행 가능 후보일입니다.";
    }

    private LocalDate parseDateOrToday(String value) {
        if (!hasText(value)) {
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

    private void appendEquals(StringBuilder sql, java.util.List<Object> args, String column, String value) {
        if (hasText(value)) {
            sql.append(" AND ").append(column).append(" = ?");
            args.add(value.trim());
        }
    }

    private List<Map<String, Object>> queryRequired(String sql, Object... args) {
        try {
            return batJdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException ex) {
            throw queryFailure(callerOperation(), ex);
        }
    }

    private String callerOperation() {
        return StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getMethodName)
                .filter(name -> !"queryRequired".equals(name) && !"callerOperation".equals(name))
                .findFirst()
                .orElse("unknownQuery"));
    }

    private BatOperationQueryException queryFailure(String operation, DataAccessException ex) {
        String transactionId;
        try {
            transactionId = CpfTransactionContext.transactionId();
        } catch (RuntimeException ignored) {
            transactionId = "N/A";
        }
        log.error("BAT 운영 조회 실패. operation={}, transactionId={}", operation, transactionId, ex);
        return new BatOperationQueryException(
                "BAT 운영 조회에 실패했습니다. operation=" + operation + ", transactionId=" + transactionId, ex);
    }

    private void recordOperation(
            String jobId,
            Long executionId,
            String operationType,
            String operatorId,
            String reason,
            String beforeData,
            String afterData) {
        batJdbcTemplate.update("""
                INSERT INTO bat_operation_log (
                    job_id, execution_id, operation_type, operator_id, reason,
                    before_data, after_data, result_type, result_message, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'S', '요청 접수', ?, ?)
                """, jobId, executionId, operationType, operatorId,
                defaultIfBlank(reason, "ADM 배치 운영 요청"), beforeData, afterData, operatorId, operatorId);
    }

    private int releaseLocksForExecution(Map<String, Object> execution) {
        String jobId = requireObjectText(execution.get("job_id"), "execution.jobId");
        String jobParameters = execution.get("job_parameters") == null ? "{}" : String.valueOf(execution.get("job_parameters"));
        String lockKey = buildLockKey(jobId, jobParameters);
        Set<String> ownerCandidates = new LinkedHashSet<>();
        addOwner(ownerCandidates, execution.get("worker_id"));
        addOwner(ownerCandidates, execution.get("server_instance_id"));
        addOwner(ownerCandidates, execution.get("batch_instance_id"));
        if (ownerCandidates.isEmpty()) {
            throw new CpfValidationException(
                    "배치 lock 소유자를 확인할 수 없어 lock을 해제하지 않았습니다. executionId=" + execution.get("execution_id"));
        }
        Map<String, Object> lock;
        try {
            lock = batJdbcTemplate.queryForMap(
                    "SELECT lock_key, owner_id FROM bat_lock WHERE lock_key = ?", lockKey);
        } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
            return 0;
        }
        String actualOwner = requireObjectText(lock.get("owner_id"), "lock.ownerId");
        if (!ownerCandidates.contains(actualOwner)) {
            throw new CpfValidationException(
                    "배치 lock의 현재 소유자가 실행 소유자와 달라 해제하지 않았습니다. lockKey=" + lockKey);
        }
        int deleted = batJdbcTemplate.update(
                "DELETE FROM bat_lock WHERE lock_key = ? AND owner_id = ?", lockKey, actualOwner);
        if (deleted != 1) {
            throw new CpfValidationException("배치 lock이 동시 변경되어 해제하지 않았습니다. lockKey=" + lockKey);
        }
        return deleted;
    }

    private static void addOwner(Set<String> owners, Object owner) {
        if (owner != null && hasText(String.valueOf(owner))) {
            owners.add(String.valueOf(owner).trim());
        }
    }

    private static String buildLockKey(String jobId, String jobParameters) {
        String normalizedJobId = requireText(jobId, "jobId");
        String normalizedParameters = hasText(jobParameters) ? jobParameters : "{}";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedParameters.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                hex.append(String.format("%02x", value));
            }
            return "batch:job:" + normalizedJobId + ":" + hex;
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256을 사용할 수 없습니다.", ex);
        }
    }

    private String normalizeGhostAction(String actionType) {
        String action = defaultIfBlank(actionType, "FAIL").trim().toUpperCase();
        if ("FAIL".equals(action) || "ABANDON".equals(action) || "RELEASE_LOCK".equals(action)) {
            return action;
        }
        throw new CpfValidationException("지원하지 않는 배치 ghost 조치 유형입니다. actionType=" + actionType);
    }

    private String yn(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback).trim().toUpperCase();
        return "Y".equals(normalized) ? "Y" : "N";
    }

    @Override
    public List<Map<String,Object>> runSchedulerOnce(String requestUser) {
        BatBatchScheduler scheduler = schedulerProvider.getIfAvailable();
        if (scheduler == null) throw new IllegalStateException("BAT Scheduler가 구성되지 않았습니다.");
        return scheduler.runOnce(requireText(requestUser, "requestUser"));
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String defaultIfBlank(String value, String fallback) {
        return hasText(value) ? value.trim() : fallback;
    }


    private static String requireObjectText(Object value, String fieldName) {
        if (value == null) {
            throw new CpfValidationException(fieldName + "은(는) 필수입니다.");
        }
        return requireText(String.valueOf(value), fieldName);
    }

    private static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new CpfValidationException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }

}
