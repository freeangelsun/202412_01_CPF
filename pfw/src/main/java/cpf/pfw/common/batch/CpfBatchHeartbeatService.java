package cpf.pfw.common.batch;

import cpf.pfw.common.logging.ServerInstanceIdentity;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * BAT 실행 중 heartbeat와 진행률을 CPF 운영 메타에 반영하는 공통 서비스입니다.
 *
 * <p>Tasklet/Chunk 구현체가 이 서비스를 직접 호출하면 긴 실행 중에도 ADM에서 worker heartbeat와
 * 처리 건수 변화를 볼 수 있습니다. listener는 시작/종료 상태를 보장하고, 장시간 진행 갱신은
 * 업무 처리 루프에서 이 서비스를 주기적으로 호출하는 방식으로 연결합니다.</p>
 */
public class CpfBatchHeartbeatService {
    public static final String PARAM_PFW_EXECUTION_ID = "cpfPfwExecutionId";

    private final CpfBatchOperationRepository repository;
    private final int heartbeatIntervalSeconds;
    private final int heartbeatTimeoutSeconds;

    public CpfBatchHeartbeatService(
            CpfBatchOperationRepository repository,
            @Value("${cpf.batch.worker.heartbeat-interval-seconds:5}") int heartbeatIntervalSeconds,
            @Value("${cpf.batch.worker.heartbeat-timeout-seconds:30}") int heartbeatTimeoutSeconds) {
        this.repository = repository;
        this.heartbeatIntervalSeconds = Math.max(1, heartbeatIntervalSeconds);
        this.heartbeatTimeoutSeconds = Math.max(this.heartbeatIntervalSeconds, heartbeatTimeoutSeconds);
    }

    public int heartbeatIntervalSeconds() {
        return heartbeatIntervalSeconds;
    }

    public int heartbeatTimeoutSeconds() {
        return heartbeatTimeoutSeconds;
    }

    public void recordJobStarted(JobExecution jobExecution) {
        Long executionId = pfwExecutionId(jobExecution);
        if (executionId == null) {
            return;
        }
        String workerId = workerId(jobExecution);
        String jobId = jobExecution.getJobInstance().getJobName();
        repository.recordWorkerHeartbeat(workerId, ServerInstanceIdentity.current(), "RUNNING", jobId, executionId, requestUser(jobExecution));
        repository.updateExecutionRuntime(
                executionId,
                jobExecution.getId(),
                workerId,
                CpfBatchRuntimeProgress.empty("RUNNING"),
                requestUser(jobExecution));
    }

    public void recordJobFinished(JobExecution jobExecution) {
        Long executionId = pfwExecutionId(jobExecution);
        if (executionId == null) {
            return;
        }
        String workerId = workerId(jobExecution);
        String status = jobExecution.getStatus().name();
        repository.recordWorkerHeartbeat(workerId, ServerInstanceIdentity.current(), "IDLE", null, null, requestUser(jobExecution));
        repository.completeExecution(
                executionId,
                status,
                jobExecution.getId(),
                workerId,
                failureMessage(jobExecution),
                jobExecution,
                requestUser(jobExecution));
    }

    public void recordStepStarted(StepExecution stepExecution) {
        Long executionId = pfwExecutionId(stepExecution.getJobExecution());
        if (executionId == null) {
            return;
        }
        String workerId = workerId(stepExecution.getJobExecution());
        CpfBatchRuntimeProgress progress = CpfBatchRuntimeProgress.of(
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                stepExecution.getStepName(),
                "RUNNING",
                "stepStarted=true");
        repository.recordWorkerHeartbeat(workerId, ServerInstanceIdentity.current(), "RUNNING",
                stepExecution.getJobExecution().getJobInstance().getJobName(), executionId, requestUser(stepExecution.getJobExecution()));
        repository.updateExecutionRuntime(executionId, stepExecution.getJobExecutionId(), workerId, progress,
                requestUser(stepExecution.getJobExecution()));
        repository.upsertStepRuntime(executionId, stepExecution.getId(), workerId, stepExecution.getStepName(), progress,
                requestUser(stepExecution.getJobExecution()));
    }

    public void recordStepFinished(StepExecution stepExecution) {
        Long executionId = pfwExecutionId(stepExecution.getJobExecution());
        if (executionId == null) {
            return;
        }
        CpfBatchRuntimeProgress stepProgress = progressFromStep(stepExecution, stepExecution.getStatus().name(), null);
        CpfBatchRuntimeProgress executionProgress = progressFromStep(stepExecution, "RUNNING", null);
        repository.upsertStepRuntime(
                executionId,
                stepExecution.getId(),
                workerId(stepExecution.getJobExecution()),
                stepExecution.getStepName(),
                stepProgress,
                requestUser(stepExecution.getJobExecution()));
        repository.updateExecutionRuntime(
                executionId,
                stepExecution.getJobExecutionId(),
                workerId(stepExecution.getJobExecution()),
                executionProgress,
                requestUser(stepExecution.getJobExecution()));
    }

    public void recordStepProgress(
            StepExecution stepExecution,
            long totalCount,
            long processedCount,
            long successCount,
            long failureCount,
            long skipCount,
            long retryCount,
            String detailMessage) {
        Long executionId = pfwExecutionId(stepExecution.getJobExecution());
        if (executionId == null) {
            return;
        }
        String workerId = workerId(stepExecution.getJobExecution());
        CpfBatchRuntimeProgress progress = CpfBatchRuntimeProgress.of(
                totalCount,
                processedCount,
                successCount,
                failureCount,
                skipCount,
                retryCount,
                elapsedMs(stepExecution.getStartTime(), null),
                stepExecution.getStepName(),
                "RUNNING",
                detailMessage);
        repository.recordWorkerHeartbeat(workerId, ServerInstanceIdentity.current(), "RUNNING",
                stepExecution.getJobExecution().getJobInstance().getJobName(), executionId, requestUser(stepExecution.getJobExecution()));
        repository.updateExecutionRuntime(executionId, stepExecution.getJobExecutionId(), workerId, progress,
                requestUser(stepExecution.getJobExecution()));
        repository.upsertStepRuntime(executionId, stepExecution.getId(), workerId, stepExecution.getStepName(), progress,
                requestUser(stepExecution.getJobExecution()));
    }

    private CpfBatchRuntimeProgress progressFromStep(StepExecution stepExecution, String status, String message) {
        long read = stepExecution.getReadCount();
        long success = stepExecution.getWriteCount();
        long skip = stepExecution.getSkipCount();
        long failure = stepExecution.getFailureExceptions().size();
        long retry = stepExecution.getRollbackCount();
        long processed = success + skip + failure;
        long total = Math.max(read, processed);
        String stepLog = message == null
                ? "commit=" + stepExecution.getCommitCount()
                + ", rollback=" + stepExecution.getRollbackCount()
                + ", readSkip=" + stepExecution.getReadSkipCount()
                + ", processSkip=" + stepExecution.getProcessSkipCount()
                + ", writeSkip=" + stepExecution.getWriteSkipCount()
                : message;
        return CpfBatchRuntimeProgress.of(
                total,
                processed,
                success,
                failure,
                skip,
                retry,
                elapsedMs(stepExecution.getStartTime(), stepExecution.getEndTime()),
                stepExecution.getStepName(),
                status,
                stepLog);
    }

    private Long pfwExecutionId(JobExecution jobExecution) {
        if (jobExecution == null || jobExecution.getJobParameters() == null) {
            return null;
        }
        return jobExecution.getJobParameters().getLong(PARAM_PFW_EXECUTION_ID);
    }

    private String workerId(JobExecution jobExecution) {
        String parameterWorkerId = jobExecution.getJobParameters().getString("serverInstanceId");
        return parameterWorkerId == null || parameterWorkerId.isBlank()
                ? ServerInstanceIdentity.current().serverInstanceId()
                : parameterWorkerId;
    }

    private String requestUser(JobExecution jobExecution) {
        String user = jobExecution.getJobParameters().getString("cpfRequestUser");
        return user == null || user.isBlank() ? "PFW_BATCH" : user;
    }

    private long elapsedMs(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            return 0L;
        }
        LocalDateTime resolvedEnd = endTime == null ? LocalDateTime.now() : endTime;
        return Math.max(0L, Duration.between(startTime, resolvedEnd).toMillis());
    }

    private String failureMessage(JobExecution jobExecution) {
        if (jobExecution == null || jobExecution.getAllFailureExceptions().isEmpty()) {
            return null;
        }
        return jobExecution.getAllFailureExceptions().toString();
    }
}
