package com.cpf.batch.edu.ondemand;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.batch.CpfBatchExecutionRequest;
import com.cpf.core.common.batch.CpfBatchExecutionResult;
import com.cpf.core.common.batch.CpfBatchLauncher;
import com.cpf.batch.common.base.BatBaseService;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.TransactionContext;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 202 접수, 멱등 저장, worker 실행, 상태·step 조회를 연결합니다. */
@Service
public class BatOnDemandService extends BatBaseService {
    private static final Map<String, String> JOB_ALLOWLIST = Map.of(
            BatOnDemandJobConfig.STANDARD_BATCH_ID, BatOnDemandJobConfig.JOB_NAME);

    private final BatOnDemandRepository repository;
    private final CpfBatchLauncher batchLauncher;
    private final ObjectProvider<JobExplorer> jobExplorerProvider;
    private final TaskExecutor taskExecutor;
    private final ObjectMapper objectMapper;

    public BatOnDemandService(
            BatOnDemandRepository repository,
            CpfBatchLauncher batchLauncher,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            @Qualifier("applicationTaskExecutor") TaskExecutor applicationTaskExecutor,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.batchLauncher = batchLauncher;
        this.jobExplorerProvider = jobExplorerProvider;
        this.taskExecutor = applicationTaskExecutor;
        this.objectMapper = objectMapper;
    }

    public BatOnDemandStatus submit(BatOnDemandRequest request) {
        String jobName = JOB_ALLOWLIST.get(request.standardBatchId());
        if (jobName == null) {
            throw new CpfValidationException("허용되지 않은 standardBatchId입니다. id=" + request.standardBatchId());
        }
        String transactionGlobalId = TransactionContext.getOrCreateTransactionId();
        BatOnDemandStatus requested = new BatOnDemandStatus(
                UUID.randomUUID().toString(), request.standardBatchId(), request.idempotencyKey(),
                transactionGlobalId, request.businessDate(), "REQUESTED", null, null,
                Map.of(), null, null, Instant.now(), null);
        BatOnDemandStatus stored = repository.createOrFind(
                requested, json(request.parameters()), request.reason(), request.requestUser());
        if (!stored.executionRequestId().equals(requested.executionRequestId())) {
            return stored;
        }
        taskExecutor.execute(() -> execute(requested, request, jobName));
        return stored;
    }

    public BatOnDemandStatus status(String executionRequestId) {
        return repository.find(executionRequestId)
                .orElseThrow(() -> new CpfNotFoundException(
                        "온디맨드 배치 접수를 찾을 수 없습니다. executionRequestId=" + executionRequestId));
    }

    public List<Map<String, Object>> steps(String executionRequestId) {
        BatOnDemandStatus status = status(executionRequestId);
        if (status.springBatchExecutionId() == null) {
            return List.of();
        }
        JobExplorer explorer = jobExplorerProvider.getIfAvailable();
        JobExecution execution = explorer == null ? null : explorer.getJobExecution(status.springBatchExecutionId());
        if (execution == null) {
            return List.of();
        }
        return execution.getStepExecutions().stream().map(this::step).toList();
    }

    public BatOnDemandStatus stop(String executionRequestId, String requestUser, String reason) {
        BatOnDemandStatus status = status(executionRequestId);
        if (status.cpfExecutionId() == null) {
            throw new CpfValidationException("아직 실행 ID가 없어 중지할 수 없습니다.");
        }
        CpfBatchExecutionResult result = batchLauncher.run(
                CpfBatchExecutionRequest.stop(status.cpfExecutionId(), requestUser, reason));
        repository.complete(executionRequestId, result.status(), result.cpfExecutionId(),
                result.springBatchExecutionId(), json(result.detail()), null, null);
        return status(executionRequestId);
    }

    public BatOnDemandStatus restart(String executionRequestId, String requestUser, String reason) {
        BatOnDemandStatus source = executableSource(executionRequestId, "재시작");
        CpfBatchExecutionResult result = batchLauncher.run(
                CpfBatchExecutionRequest.restart(source.cpfExecutionId(), requestUser, reason));
        repository.complete(executionRequestId, result.status(), result.cpfExecutionId(),
                result.springBatchExecutionId(), json(result.detail()),
                result.executed() ? null : result.status(), result.executed() ? null : result.message());
        return status(executionRequestId);
    }

    public BatOnDemandStatus rerun(String executionRequestId, String requestUser, String reason) {
        BatOnDemandStatus source = executableSource(executionRequestId, "재수행");
        CpfBatchExecutionResult result = batchLauncher.run(
                CpfBatchExecutionRequest.rerun(source.cpfExecutionId(), requestUser, reason));
        repository.complete(executionRequestId, result.status(), result.cpfExecutionId(),
                result.springBatchExecutionId(), json(result.detail()),
                result.executed() ? null : result.status(), result.executed() ? null : result.message());
        return status(executionRequestId);
    }

    private void execute(BatOnDemandStatus requested, BatOnDemandRequest request, String jobName) {
        try {
            TransactionContext.initialize(requested.transactionGlobalId(), null, null);
            repository.markRunning(requested.executionRequestId());
            CpfBatchExecutionResult result = batchLauncher.run(CpfBatchExecutionRequest.onDemand(
                    request.standardBatchId(), jobName, request.businessDate(), request.idempotencyKey(),
                    json(request.parameters()), request.requestUser(), request.reason()));
            repository.complete(requested.executionRequestId(), result.status(), result.cpfExecutionId(),
                    result.springBatchExecutionId(), json(result.detail()), null, null);
        } catch (RuntimeException ex) {
            repository.complete(requested.executionRequestId(), "FAILED", null, null,
                    "{}", ex.getClass().getSimpleName(), safeMessage(ex));
        } finally {
            TransactionContext.clear();
        }
    }

    private BatOnDemandStatus executableSource(String executionRequestId, String operationName) {
        BatOnDemandStatus source = status(executionRequestId);
        if (source.cpfExecutionId() == null) {
            throw new CpfValidationException(operationName + " 기준 실행 ID가 없습니다.");
        }
        if ("REQUESTED".equals(source.requestStatus()) || "RUNNING".equals(source.requestStatus())) {
            throw new CpfValidationException("실행 중인 배치는 " + operationName + "할 수 없습니다.");
        }
        return source;
    }

    private Map<String, Object> step(StepExecution execution) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("stepExecutionId", execution.getId());
        value.put("stepName", execution.getStepName());
        value.put("status", execution.getStatus().name());
        value.put("readCount", execution.getReadCount());
        value.put("writeCount", execution.getWriteCount());
        value.put("skipCount", execution.getSkipCount());
        value.put("startTime", execution.getStartTime());
        value.put("endTime", execution.getEndTime());
        return value;
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value == null ? Map.of() : value);
        } catch (JsonProcessingException ex) {
            throw new CpfValidationException("배치 파라미터를 JSON으로 변환할 수 없습니다.");
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        return message == null ? ex.getClass().getSimpleName() : message.substring(0, Math.min(message.length(), 1000));
    }
}
