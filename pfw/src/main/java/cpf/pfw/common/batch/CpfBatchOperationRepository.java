package cpf.pfw.common.batch;

import cpf.pfw.common.logging.SensitiveDataMasker;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * PFW 배치 운영 메타 저장소입니다.
 *
 * <p>Spring Batch 표준 BATCH_* 테이블과 별개로 ADM 관제에 필요한 CPF 운영 메타를
 * pfw_batch_* 테이블에 기록합니다.</p>
 */
public class CpfBatchOperationRepository {
    private final ObjectProvider<JdbcTemplate> jdbcTemplateProvider;
    private final ObjectProvider<DataSource> dataSourceProvider;

    public CpfBatchOperationRepository(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        this.jdbcTemplateProvider = jdbcTemplateProvider;
        this.dataSourceProvider = dataSourceProvider;
    }

    public boolean available() {
        return jdbcTemplateProvider.getIfAvailable() != null || dataSourceProvider.getIfAvailable() != null;
    }

    public void ensureJob(String jobId, String jobName, String jobType, String requestUser) {
        if (!available()) {
            return;
        }
        String user = defaultIfBlank(requestUser, "PFW_BATCH");
        jdbc().update("""
                INSERT INTO pfw_batch_job (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
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

    public long insertExecution(
            CpfBatchExecutionRequest request,
            String status,
            Long springBatchExecutionId,
            String batchInstanceId,
            String errorMessage,
            JobExecution jobExecution) {
        if (!available()) {
            return -1L;
        }
        String user = request.normalizedRequestUser("PFW_BATCH");
        BatchCounts counts = BatchCounts.from(jobExecution);
        jdbc().update("""
                INSERT INTO pfw_batch_execution (
                    job_id, schedule_id, job_parameters, execution_status, spring_batch_execution_id,
                    batch_instance_id, start_time, end_time, read_count, write_count, skip_count,
                    error_message, requested_by, created_by, updated_by
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                request.requiredJobId(),
                blankToNull(request.scheduleId()),
                request.normalizedJobParameters(),
                required(status, "status"),
                springBatchExecutionId,
                batchInstanceId,
                toTimestamp(jobExecution == null ? null : jobExecution.getStartTime()),
                toTimestamp(jobExecution == null ? null : jobExecution.getEndTime()),
                counts.readCount(),
                counts.writeCount(),
                counts.skipCount(),
                SensitiveDataMasker.mask(errorMessage, 1000),
                user,
                user,
                user);
        Long executionId = jdbc().queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (executionId == null) {
            throw new IllegalStateException("CPF 배치 실행 ID를 확인할 수 없습니다.");
        }
        if (jobExecution != null) {
            insertStepExecutions(executionId, jobExecution, user);
        }
        return executionId;
    }

    public Map<String, Object> findExecution(long executionId) {
        return jdbc().queryForMap("""
                SELECT execution_id, job_id, schedule_id, job_parameters, execution_status,
                       spring_batch_execution_id, batch_instance_id,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, requested_by, created_at, updated_at
                FROM pfw_batch_execution
                WHERE execution_id = ?
                """, executionId);
    }

    public Map<String, Object> findExecutionDetail(long executionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        Map<String, Object> execution = findExecution(executionId);
        List<Map<String, Object>> steps = queryForList("""
                SELECT step_execution_id, execution_id, step_name, execution_status,
                       start_time, end_time, read_count, write_count, skip_count,
                       error_message, step_log, created_at, updated_at
                FROM pfw_batch_step_execution
                WHERE execution_id = ?
                ORDER BY step_execution_id
                """, executionId);
        result.put("execution", execution);
        result.put("steps", steps);
        return result;
    }

    public void updateExecutionStatus(long executionId, String status, String requestUser) {
        jdbc().update("""
                UPDATE pfw_batch_execution
                SET execution_status = ?,
                    updated_by = ?,
                    updated_at = CURRENT_TIMESTAMP
                WHERE execution_id = ?
                """,
                required(status, "status"),
                defaultIfBlank(requestUser, "PFW_BATCH"),
                executionId);
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
        String user = defaultIfBlank(operatorId, "PFW_BATCH");
        jdbc().update("""
                INSERT INTO pfw_batch_operation_log (
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

    private void insertStepExecutions(long pfwExecutionId, JobExecution jobExecution, String user) {
        for (StepExecution step : jobExecution.getStepExecutions()) {
            jdbc().update("""
                    INSERT INTO pfw_batch_step_execution (
                        execution_id, step_name, execution_status, start_time, end_time,
                        read_count, write_count, skip_count, error_message, step_log, created_by, updated_by
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    pfwExecutionId,
                    step.getStepName(),
                    step.getStatus().name(),
                    toTimestamp(step.getStartTime()),
                    toTimestamp(step.getEndTime()),
                    step.getReadCount(),
                    step.getWriteCount(),
                    step.getSkipCount(),
                    SensitiveDataMasker.mask(step.getFailureExceptions().isEmpty() ? null : step.getFailureExceptions().toString(), 1000),
                    "commit=" + step.getCommitCount()
                            + ", rollback=" + step.getRollbackCount()
                            + ", readSkip=" + step.getReadSkipCount()
                            + ", processSkip=" + step.getProcessSkipCount()
                            + ", writeSkip=" + step.getWriteSkipCount(),
                    user,
                    user);
        }
    }

    private JdbcTemplate jdbc() {
        JdbcTemplate jdbcTemplate = jdbcTemplateProvider.getIfAvailable();
        if (jdbcTemplate != null) {
            return jdbcTemplate;
        }
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("PFW datasource가 없어 배치 운영 메타를 사용할 수 없습니다.");
        }
        return new JdbcTemplate(dataSource);
    }

    private Timestamp toTimestamp(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
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

    private record BatchCounts(long readCount, long writeCount, long skipCount) {
        static BatchCounts from(JobExecution execution) {
            if (execution == null) {
                return new BatchCounts(0, 0, 0);
            }
            long read = execution.getStepExecutions().stream().mapToLong(StepExecution::getReadCount).sum();
            long write = execution.getStepExecutions().stream().mapToLong(StepExecution::getWriteCount).sum();
            long skip = execution.getStepExecutions().stream().mapToLong(StepExecution::getSkipCount).sum();
            return new BatchCounts(read, write, skip);
        }
    }
}
