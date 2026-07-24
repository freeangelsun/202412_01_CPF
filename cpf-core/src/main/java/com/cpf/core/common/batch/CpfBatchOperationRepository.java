package com.cpf.core.common.batch;

import com.cpf.core.common.logging.SensitiveDataMasker;
import com.cpf.core.common.logging.ServerInstanceIdentity;
import com.cpf.core.common.logging.file.CpfFileLogWriter;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CPF 배치 운영 메타 저장소입니다.
 *
 * <p>Spring Batch 표준 BATCH_* 테이블은 실행 원천 이력으로 유지하고, ADM 관제에 필요한
 * worker heartbeat, 진행률, ghost 후보, 운영 조치 이력은 bat_* 테이블에 저장합니다.</p>
 */
public class CpfBatchOperationRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;
    private final CpfFileLogWriter fileLogWriter;

    public CpfBatchOperationRepository(
            @Qualifier("batJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("batDataSource") ObjectProvider<DataSource> dataSourceProvider,
            CpfFileLogWriter fileLogWriter) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
        this.fileLogWriter = fileLogWriter;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public void ensureJob(String jobId, String jobName, String jobType, String requestUser) {
        if (!available()) {
            return;
        }
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        jdbc().update("""
                INSERT INTO bat_job (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
                VALUES (?, ?, ?, ?, 'Y', 'Y', ?, ?)
                ON DUPLICATE KEY UPDATE
                    job_name = VALUES(job_name),
                    job_type = VALUES(job_type),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                required(jobId, "jobId"),
                defaultIfBlank(jobName, jobId),
                defaultIfBlank(jobType, "TASKLET"),
                "Spring Batch Job bean 자동 등록",
                user,
                user);
    }

    public long startExecution(
            CpfBatchExecutionRequest request,
            String status,
            Long springBatchExecutionId,
            String batchInstanceId,
            String serverInstanceId,
            String workerId,
            String transactionGlobalId,
            String requestUser) {
        if (!available()) {
            return -1L;
        }
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        ensureBatchInstance(batchInstanceId, serverInstanceId, user);
        jdbc().update("""
                INSERT INTO bat_execution (
                    job_id, schedule_id, job_parameters, execution_status, spring_batch_execution_id,
                    batch_instance_id, server_instance_id, worker_id, transaction_global_id,
                    start_time, end_time, read_count, write_count, skip_count,
                    error_message, requested_by, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), NULL, 0, 0, 0, NULL, ?, ?, ?)
                """,
                request.requiredJobId(),
                blankToNull(request.scheduleId()),
                request.normalizedJobParameters(),
                required(status, "status"),
                springBatchExecutionId,
                batchInstanceId,
                serverInstanceId,
                workerId,
                transactionGlobalId,
                user,
                user,
                user);
        Long executionId = jdbc().queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (executionId == null) {
            throw new IllegalStateException("CPF 배치 실행 ID를 확인할 수 없습니다.");
        }
        tryUpdateExecutionExtendedMetrics(
                executionId,
                CpfBatchRuntimeProgress.empty(status),
                springBatchExecutionId,
                workerId,
                user);
        return executionId;
    }

    public long insertExecution(
            CpfBatchExecutionRequest request,
            String status,
            Long springBatchExecutionId,
            String batchInstanceId,
            String serverInstanceId,
            String workerId,
            String transactionGlobalId,
            String errorMessage,
            JobExecution jobExecution) {
        long executionId = startExecution(
                request,
                status,
                springBatchExecutionId,
                batchInstanceId,
                serverInstanceId,
                workerId,
                transactionGlobalId,
                request.normalizedRequestUser("CPF_BATCH"));
        completeExecution(executionId, status, springBatchExecutionId, workerId, errorMessage, jobExecution,
                request.normalizedRequestUser("CPF_BATCH"));
        return executionId;
    }

    public void completeExecution(
            long executionId,
            String status,
            Long springBatchExecutionId,
            String workerId,
            String errorMessage,
            JobExecution jobExecution,
            String requestUser) {
        if (!available() || executionId < 1) {
            return;
        }
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        BatchCounts counts = BatchCounts.from(jobExecution);
        jdbc().update("""
                UPDATE bat_execution
                SET execution_status = ?,
                    spring_batch_execution_id = COALESCE(?, spring_batch_execution_id),
                    end_time = COALESCE(?, CURRENT_TIMESTAMP(3)),
                    read_count = ?,
                    write_count = ?,
                    skip_count = ?,
                    error_message = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """,
                required(status, "status"),
                springBatchExecutionId,
                toTimestamp(jobExecution == null ? null : jobExecution.getEndTime()),
                counts.readCount(),
                counts.writeCount(),
                counts.skipCount(),
                SensitiveDataMasker.mask(errorMessage, 1000),
                user,
                executionId);
        CpfBatchRuntimeProgress progress = counts.toProgress(status, jobExecution);
        tryUpdateExecutionExtendedMetrics(executionId, progress, springBatchExecutionId, workerId, user);
        if (jobExecution != null) {
            tryUpdateExecutionLogLinkage(executionId, jobExecution, user);
            upsertStepExecutions(executionId, jobExecution, workerId, user);
        }
    }

    public Map<String, Object> findExecution(long executionId) {
        return jdbc().queryForMap("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, batch_instance_id, server_instance_id,
                       worker_id, transaction_global_id,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, requested_by, created_at, updated_at
                FROM bat_execution
                WHERE execution_id = ?
                """, executionId);
    }

    public Map<String, Object> findExecutionDetail(long executionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> execution = findExecution(executionId);
        List<Map<String, Object>> steps = queryForList("""
                SELECT step_execution_id, execution_id, spring_batch_step_execution_id, worker_id,
                       step_name, execution_status,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, step_log, created_at, updated_at
                FROM bat_step_execution
                WHERE execution_id = ?
                ORDER BY step_execution_id
                """, executionId);
        result.put("execution", execution);
        result.put("steps", steps);
        result.put("extendedExecution", findExecutionExtendedMetrics(executionId));
        result.put("logLinkage", findExecutionLogLinkage(executionId));
        result.put("extendedSteps", findStepExtendedMetrics(executionId));
        return result;
    }

    public void updateExecutionStatus(long executionId, String status, String requestUser) {
        jdbc().update("""
                UPDATE bat_execution
                SET execution_status = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """,
                required(status, "status"),
                defaultIfBlank(requestUser, "CPF_BATCH"),
                executionId);
    }

    public void updateExecutionRuntime(
            long executionId,
            Long springBatchExecutionId,
            String workerId,
            CpfBatchRuntimeProgress progress,
            String requestUser) {
        if (!available() || executionId < 1) {
            return;
        }
        CpfBatchRuntimeProgress resolved = progress == null
                ? CpfBatchRuntimeProgress.empty("RUNNING")
                : progress;
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        jdbc().update("""
                UPDATE bat_execution
                SET execution_status = ?,
                    spring_batch_execution_id = COALESCE(?, spring_batch_execution_id),
                    read_count = ?,
                    write_count = ?,
                    skip_count = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """,
                resolved.status(),
                springBatchExecutionId,
                resolved.totalCount(),
                resolved.successCount(),
                resolved.skipCount(),
                user,
                executionId);
        tryUpdateExecutionExtendedMetrics(executionId, resolved, springBatchExecutionId, workerId, user);
    }

    public void upsertStepRuntime(
            long executionId,
            Long springBatchStepExecutionId,
            String workerId,
            String stepName,
            CpfBatchRuntimeProgress progress,
            String requestUser) {
        if (!available() || executionId < 1 || stepName == null || stepName.isBlank()) {
            return;
        }
        CpfBatchRuntimeProgress resolved = progress == null
                ? CpfBatchRuntimeProgress.empty("RUNNING")
                : progress;
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        int updated = jdbc().update("""
                UPDATE bat_step_execution
                SET spring_batch_step_execution_id = COALESCE(?, spring_batch_step_execution_id),
                    worker_id = ?,
                    execution_status = ?,
                    read_count = ?,
                    write_count = ?,
                    skip_count = ?,
                    step_log = CASE
                        WHEN ? IS NULL THEN step_log
                        WHEN step_log IS NULL OR step_log = '' THEN ?
                        WHEN step_log LIKE CONCAT('%', ?, '%') THEN step_log
                        ELSE CONCAT(step_log, '; ', ?)
                    END,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                  AND step_name = ?
                """,
                springBatchStepExecutionId,
                blankToNull(workerId),
                resolved.status(),
                resolved.totalCount(),
                resolved.successCount(),
                resolved.skipCount(),
                resolved.stepLog(),
                resolved.stepLog(),
                resolved.stepLog(),
                resolved.stepLog(),
                user,
                executionId,
                stepName);
        if (updated == 0) {
            jdbc().update("""
                    INSERT INTO bat_step_execution (
                        execution_id, spring_batch_step_execution_id, worker_id, step_name, execution_status,
                        start_time, end_time, read_count, write_count, skip_count,
                        error_message, step_log, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP(3), NULL, ?, ?, ?, NULL, ?, ?, ?)
                    """,
                    executionId,
                    springBatchStepExecutionId,
                    blankToNull(workerId),
                    stepName,
                    resolved.status(),
                    resolved.totalCount(),
                    resolved.successCount(),
                    resolved.skipCount(),
                    resolved.stepLog(),
                    user,
                    user);
        }
        tryUpdateStepExtendedMetrics(executionId, stepName, resolved, user);
    }

    public void recordWorkerHeartbeat(
            String workerId,
            ServerInstanceIdentity.Identity identity,
            String workerStatus,
            String currentJobId,
            Long currentExecutionId,
            String requestUser) {
        if (!available()) {
            return;
        }
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        String resolvedWorkerId = required(workerId, "workerId");
        ServerInstanceIdentity.Identity resolvedIdentity = identity == null
                ? ServerInstanceIdentity.current()
                : identity;
        try {
            jdbc().update("""
                    INSERT INTO bat_worker (
                        worker_id, server_instance_id, host_name, process_id, thread_name, worker_status,
                        active_yn, last_heartbeat_at, current_job_id, current_execution_id, description,
                        created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, 'Y', CURRENT_TIMESTAMP(3), ?, ?, 'CPF 배치 실행기 heartbeat', ?, ?)
                    ON DUPLICATE KEY UPDATE
                        server_instance_id = VALUES(server_instance_id),
                        host_name = VALUES(host_name),
                        process_id = VALUES(process_id),
                        thread_name = VALUES(thread_name),
                        worker_status = VALUES(worker_status),
                        active_yn = 'Y',
                        last_heartbeat_at = CURRENT_TIMESTAMP(3),
                        current_job_id = VALUES(current_job_id),
                        current_execution_id = VALUES(current_execution_id),
                        updated_by = VALUES(updated_by),
                        updated_at = CURRENT_TIMESTAMP
                    """,
                    resolvedWorkerId,
                    resolvedIdentity.serverInstanceId(),
                    resolvedIdentity.hostName(),
                    resolvedIdentity.processId(),
                    resolvedIdentity.threadName(),
                    defaultIfBlank(workerStatus, "IDLE"),
                    blankToNull(currentJobId),
                    currentExecutionId,
                    user,
                    user);
        } catch (DataAccessException ignored) {
            // 오래된 DB에서도 배치 실행 자체는 계속 가능해야 하므로 heartbeat 저장 실패는 관제 보강 대상으로 남깁니다.
        }
    }

    public int detectGhostCandidates(int heartbeatTimeoutSeconds, String requestUser) {
        if (!available()) {
            return 0;
        }
        int timeoutSeconds = Math.max(1, heartbeatTimeoutSeconds);
        String user = defaultIfBlank(requestUser, "CPF_GHOST_DETECTOR");
        try {
            return jdbc().update("""
                    INSERT INTO bat_ghost_event (
                        execution_id, spring_batch_execution_id, job_id, server_instance_id, worker_id,
                        ghost_status, detected_reason, lock_released_yn, retryable_yn,
                        before_data, created_by, updated_by
                    )
                    SELECT e.execution_id,
                           e.spring_batch_execution_id,
                           e.job_id,
                           e.server_instance_id,
                           e.worker_id,
                           'DETECTED',
                           CASE
                               WHEN w.worker_id IS NULL THEN '실행 worker heartbeat가 없습니다.'
                               WHEN w.last_heartbeat_at IS NULL THEN 'worker heartbeat 시각이 없습니다.'
                               WHEN TIMESTAMPDIFF(SECOND, w.last_heartbeat_at, CURRENT_TIMESTAMP(3)) > ?
                                   THEN 'worker heartbeat timeout을 초과했습니다.'
                               ELSE '실행 메타 heartbeat timeout을 초과했습니다.'
                           END,
                           'N',
                           'Y',
                           CONCAT('executionStatus=', e.execution_status,
                                  ', executionUpdatedAt=', COALESCE(CAST(e.updated_at AS CHAR), ''),
                                  ', workerHeartbeatAt=', COALESCE(CAST(w.last_heartbeat_at AS CHAR), '')),
                           ?,
                           ?
                    FROM bat_execution e
                    LEFT JOIN bat_worker w ON w.worker_id = e.worker_id
                    WHERE e.end_time IS NULL
                      AND e.execution_status IN ('REQUESTED', 'STARTING', 'STARTED', 'RUNNING', 'UNKNOWN', 'STOPPING')
                      AND (
                          w.worker_id IS NULL
                          OR w.last_heartbeat_at IS NULL
                          OR TIMESTAMPDIFF(SECOND, w.last_heartbeat_at, CURRENT_TIMESTAMP(3)) > ?
                          OR TIMESTAMPDIFF(SECOND, COALESCE(e.updated_at, e.start_time, e.created_at), CURRENT_TIMESTAMP(3)) > ?
                      )
                      AND NOT EXISTS (
                          SELECT 1
                          FROM bat_ghost_event g
                          WHERE g.execution_id = e.execution_id
                            AND g.ghost_status = 'DETECTED'
                      )
                    """,
                    timeoutSeconds,
                    user,
                    user,
                    timeoutSeconds,
                    timeoutSeconds);
        } catch (DataAccessException ignored) {
            return 0;
        }
    }

    public void recordOperation(
            String jobId,
            Long executionId,
            String operationType,
            String operatorId,
            String reason,
            String beforeData,
            String afterData,
            String resultType,
            String resultMessage) {
        if (!available()) {
            return;
        }
        String user = defaultIfBlank(operatorId, "CPF_BATCH");
        jdbc().update("""
                INSERT INTO bat_operation_log (
                    job_id, execution_id, operation_type, operator_id, reason,
                    before_data, after_data, result_type, result_message, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                required(jobId, "jobId"),
                executionId,
                required(operationType, "operationType"),
                user,
                defaultIfBlank(reason, "배치 운영 요청"),
                SensitiveDataMasker.mask(beforeData, 3000),
                SensitiveDataMasker.mask(afterData, 3000),
                defaultIfBlank(resultType, "S"),
                SensitiveDataMasker.mask(defaultIfBlank(resultMessage, "요청 접수"), 1000),
                user,
                user);
    }

    public List<Map<String, Object>> queryForList(String sql, Object... args) {
        try {
            return jdbc().queryForList(sql, args);
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private void ensureBatchInstance(String batchInstanceId, String serverInstanceId, String requestUser) {
        if (batchInstanceId == null || batchInstanceId.isBlank()) {
            return;
        }
        String user = defaultIfBlank(requestUser, "CPF_BATCH");
        String instanceName = defaultIfBlank(serverInstanceId, batchInstanceId);
        jdbc().update("""
                INSERT INTO bat_instance (
                    instance_id, instance_name, host_name, server_port, active_yn,
                    last_heartbeat_at, description, created_by, updated_by
                ) VALUES (?, ?, ?, NULL, 'Y', CURRENT_TIMESTAMP(3), ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    instance_name = VALUES(instance_name),
                    active_yn = 'Y',
                    last_heartbeat_at = CURRENT_TIMESTAMP(3),
                    description = VALUES(description),
                    updated_by = VALUES(updated_by),
                    updated_at = CURRENT_TIMESTAMP
                """,
                batchInstanceId,
                instanceName,
                instanceName,
                "CPF Batch 공통 API가 자동 보장한 실행 인스턴스",
                user,
                user);
    }

    private void upsertStepExecutions(long cpfExecutionId, JobExecution jobExecution, String workerId, String user) {
        for (StepExecution step : jobExecution.getStepExecutions()) {
            CpfBatchRuntimeProgress progress = StepCounts.from(step).toProgress(step.getStatus().name(), step);
            upsertStepRuntime(
                    cpfExecutionId,
                    step.getId(),
                    workerId,
                    step.getStepName(),
                    progress,
                    user);
            jdbc().update("""
                    UPDATE bat_step_execution
                    SET end_time = ?,
                        error_message = ?,
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                      AND step_name = ?
                    """,
                    toTimestamp(step.getEndTime()),
                    SensitiveDataMasker.mask(step.getFailureExceptions().isEmpty() ? null : step.getFailureExceptions().toString(), 1000),
                    user,
                    cpfExecutionId,
                    step.getStepName());
        }
    }

    private void tryUpdateExecutionExtendedMetrics(
            long executionId,
            CpfBatchRuntimeProgress progress,
            Long springBatchExecutionId,
            String workerId,
            String user) {
        try {
            jdbc().update("""
                    UPDATE bat_execution
                    SET total_count = ?,
                        processed_count = ?,
                        success_count = ?,
                        failure_count = ?,
                        retry_count = ?,
                        progress_rate = ?,
                        tps = ?,
                        avg_elapsed_ms = ?,
                        max_elapsed_ms = ?,
                        last_heartbeat_at = CURRENT_TIMESTAMP(3),
                        current_step_name = ?,
                        spring_batch_execution_id = COALESCE(?, spring_batch_execution_id),
                        worker_id = COALESCE(?, worker_id),
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                    """,
                    progress.totalCount(),
                    progress.processedCount(),
                    progress.successCount(),
                    progress.failureCount(),
                    progress.retryCount(),
                    progress.progressRate(),
                    progress.tps(),
                    progress.avgElapsedMs(),
                    progress.maxElapsedMs(),
                    progress.currentStepName(),
                    springBatchExecutionId,
                    blankToNull(workerId),
                    user,
                    executionId);
        } catch (DataAccessException ignored) {
            // 확장 칼럼 migration 전 DB에서는 기존 count와 updated_at만으로 호환 동작합니다.
        }
    }

    private void tryUpdateExecutionLogLinkage(long executionId, JobExecution jobExecution, String user) {
        if (jobExecution == null || jobExecution.getJobInstance() == null) {
            return;
        }
        try {
            String businessDateText = executionContextValue(
                    jobExecution,
                    CpfBatchFileLogWriter.CONTEXT_BUSINESS_DATE,
                    jobExecution.getJobParameters().getString("businessDate"));
            LocalDate businessDate = businessDateText == null
                    ? null
                    : parseBusinessDate(businessDateText);
            long jobInstanceId = jobExecution.getJobInstance().getInstanceId();
            String relativePath = businessDate == null || jobInstanceId < 1
                    ? null
                    : fileLogWriter.relativeToLogRoot(fileLogWriter.batchJobLogPath(
                            CpfBatchJobLogPath.relativePath(
                                    jobExecution.getJobInstance().getJobName(),
                                    jobInstanceId,
                                    businessDate))).toString().replace('\\', '/');
            jdbc().update("""
                    UPDATE bat_execution
                    SET spring_batch_job_instance_id = ?,
                        business_date = ?,
                        run_id = ?,
                        rerun_id = ?,
                        original_job_execution_id = ?,
                        restart_attempt = ?,
                        parent_transaction_global_id = ?,
                        transaction_segment_id = ?,
                        parent_segment_id = ?,
                        job_log_relative_path = ?,
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                    """,
                    jobInstanceId,
                    businessDate == null ? null : java.sql.Date.valueOf(businessDate),
                    jobExecution.getJobParameters().getString("runId"),
                    jobExecution.getJobParameters().getString("rerunId"),
                    nullableLong(executionContextValue(
                            jobExecution,
                            CpfBatchFileLogWriter.CONTEXT_ORIGINAL_JOB_EXECUTION_ID,
                            jobExecution.getJobParameters().getString("originalJobExecutionId"))),
                    executionContextLong(jobExecution, CpfBatchFileLogWriter.CONTEXT_RESTART_ATTEMPT, 0L),
                    executionContextValue(jobExecution, CpfBatchFileLogWriter.CONTEXT_PARENT_TRANSACTION_GLOBAL_ID, null),
                    executionContextValue(jobExecution, CpfBatchFileLogWriter.CONTEXT_SEGMENT_ID, null),
                    executionContextValue(jobExecution, CpfBatchFileLogWriter.CONTEXT_PARENT_SEGMENT_ID, null),
                    relativePath,
                    user,
                    executionId);
        } catch (DataAccessException | IllegalArgumentException ignored) {
            // V24 적용 전 DB에서도 기존 배치 실행은 유지하고 로그 연계 정보만 비워 둡니다.
        }
    }

    private void tryUpdateStepExtendedMetrics(
            long executionId,
            String stepName,
            CpfBatchRuntimeProgress progress,
            String user) {
        try {
            jdbc().update("""
                    UPDATE bat_step_execution
                    SET total_count = ?,
                        processed_count = ?,
                        success_count = ?,
                        failure_count = ?,
                        retry_count = ?,
                        progress_rate = ?,
                        tps = ?,
                        avg_elapsed_ms = ?,
                        max_elapsed_ms = ?,
                        last_heartbeat_at = CURRENT_TIMESTAMP(3),
                        updated_by = ?,
                        updated_at = CURRENT_TIMESTAMP
                    WHERE execution_id = ?
                      AND step_name = ?
                    """,
                    progress.totalCount(),
                    progress.processedCount(),
                    progress.successCount(),
                    progress.failureCount(),
                    progress.retryCount(),
                    progress.progressRate(),
                    progress.tps(),
                    progress.avgElapsedMs(),
                    progress.maxElapsedMs(),
                    user,
                    executionId,
                    stepName);
        } catch (DataAccessException ignored) {
            // 확장 칼럼 migration 전 DB에서는 기존 step count와 updated_at만으로 호환 동작합니다.
        }
    }

    private Map<String, Object> findExecutionExtendedMetrics(long executionId) {
        try {
            return jdbc().queryForMap("""
                    SELECT total_count, processed_count, success_count, failure_count, retry_count,
                           progress_rate, tps, avg_elapsed_ms, max_elapsed_ms,
                           last_heartbeat_at, current_step_name
                    FROM bat_execution
                    WHERE execution_id = ?
                    """, executionId);
        } catch (DataAccessException ignored) {
            return Map.of();
        }
    }

    private Map<String, Object> findExecutionLogLinkage(long executionId) {
        try {
            return jdbc().queryForMap("""
                    SELECT spring_batch_job_instance_id, business_date, run_id, rerun_id,
                           original_job_execution_id, restart_attempt,
                           parent_transaction_global_id, transaction_segment_id, parent_segment_id,
                           job_log_relative_path
                    FROM bat_execution
                    WHERE execution_id = ?
                    """, executionId);
        } catch (DataAccessException ignored) {
            return Map.of();
        }
    }

    private List<Map<String, Object>> findStepExtendedMetrics(long executionId) {
        try {
            return jdbc().queryForList("""
                    SELECT step_execution_id, step_name, total_count, processed_count, success_count,
                           failure_count, retry_count, progress_rate, tps, avg_elapsed_ms,
                           max_elapsed_ms, last_heartbeat_at
                    FROM bat_step_execution
                    WHERE execution_id = ?
                    ORDER BY step_execution_id
                    """, executionId);
        } catch (DataAccessException ignored) {
            return List.of();
        }
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("BAT datasource가 없어 배치 운영 메타를 사용할 수 없습니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    private String executionContextValue(JobExecution execution, String key, String fallback) {
        if (execution != null && execution.getExecutionContext() != null
                && execution.getExecutionContext().containsKey(key)) {
            Object value = execution.getExecutionContext().get(key);
            if (value != null && !String.valueOf(value).isBlank()) {
                return String.valueOf(value);
            }
        }
        return fallback;
    }

    private long executionContextLong(JobExecution execution, String key, long fallback) {
        String value = executionContextValue(execution, key, null);
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private Long nullableLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private LocalDate parseBusinessDate(String value) {
        return value.contains("-")
                ? LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
                : LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE);
    }

    private String required(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + "은 필수입니다.");
        }
        return value.trim();
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private long elapsedMs(LocalDateTime start, LocalDateTime end) {
        if (start == null) {
            return 0;
        }
        LocalDateTime resolvedEnd = end == null ? LocalDateTime.now() : end;
        return Math.max(0, java.time.Duration.between(start, resolvedEnd).toMillis());
    }

    private record BatchCounts(long readCount, long writeCount, long skipCount, long failureCount, long retryCount) {
        static BatchCounts from(JobExecution execution) {
            if (execution == null) {
                return new BatchCounts(0, 0, 0, 0, 0);
            }
            long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
            long write = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
            long skip = execution.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();
            long failure = execution.getAllFailureExceptions().size();
            long retry = execution.getStepExecutions().stream().mapToLong(StepExecution::getRollbackCount).sum();
            return new BatchCounts(read, write, skip, failure, retry);
        }

        CpfBatchRuntimeProgress toProgress(String status, JobExecution execution) {
            long processed = writeCount + skipCount + failureCount;
            long total = Math.max(readCount, processed);
            return CpfBatchRuntimeProgress.of(
                    total,
                    processed,
                    writeCount,
                    failureCount,
                    skipCount,
                    retryCount,
                    execution == null ? 0 : elapsedMsStatic(execution.getStartTime(), execution.getEndTime()),
                    null,
                    status,
                    null);
        }

        private static long elapsedMsStatic(LocalDateTime start, LocalDateTime end) {
            if (start == null) {
                return 0;
            }
            LocalDateTime resolvedEnd = end == null ? LocalDateTime.now() : end;
            return Math.max(0, java.time.Duration.between(start, resolvedEnd).toMillis());
        }
    }

    private record StepCounts(long readCount, long writeCount, long skipCount, long failureCount, long retryCount) {
        static StepCounts from(StepExecution step) {
            if (step == null) {
                return new StepCounts(0, 0, 0, 0, 0);
            }
            return new StepCounts(
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    step.getFailureExceptions().size(),
                    step.getRollbackCount());
        }

        CpfBatchRuntimeProgress toProgress(String status, StepExecution step) {
            long processed = writeCount + skipCount + failureCount;
            long total = Math.max(readCount, processed);
            String stepLog = step == null ? null : "commit=" + step.getCommitCount()
                    + ", rollback=" + step.getRollbackCount()
                    + ", readSkip=" + step.getReadSkipCount()
                    + ", processSkip=" + step.getProcessSkipCount()
                    + ", writeSkip=" + step.getWriteSkipCount();
            long elapsedMs = step == null ? 0 : elapsedMsStatic(step.getStartTime(), step.getEndTime());
            return CpfBatchRuntimeProgress.of(
                    total,
                    processed,
                    writeCount,
                    failureCount,
                    skipCount,
                    retryCount,
                    elapsedMs,
                    step == null ? null : step.getStepName(),
                    status,
                    stepLog);
        }

        private static long elapsedMsStatic(LocalDateTime start, LocalDateTime end) {
            if (start == null) {
                return 0;
            }
            LocalDateTime resolvedEnd = end == null ? LocalDateTime.now() : end;
            return Math.max(0, java.time.Duration.between(start, resolvedEnd).toMillis());
        }
    }
}
