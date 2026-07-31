package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;

/** Remote Chunk의 업무 Side Effect를 Writer Transaction 안에서 실행합니다. */
public final class CpfRemoteChunkItemWriter implements ItemWriter<Map<String, Object>> {
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;

    public CpfRemoteChunkItemWriter(CpfBatchStepHandlerRegistry handlers, BatchFencingPort fencing) {
        this.handlers = handlers;
        this.fencing = fencing;
    }

    @Override
    public void write(Chunk<? extends Map<String, Object>> chunk) throws Exception {
        var context = StepSynchronizationManager.getContext();
        if (context == null) throw new IllegalStateException("BATCH_REMOTE_CHUNK_STEP_CONTEXT_MISSING");
        StepExecution execution = context.getStepExecution();
        JobParameters parameters = execution.getJobExecution().getJobParameters();
        String cpfExecutionId = CpfRemoteChunkItemProcessor.required(
                parameters.getString("cpfExecutionId"), "cpfExecutionId");
        String jobId = CpfRemoteChunkItemProcessor.required(parameters.getString("jobId"), "jobId");
        long fencingToken = CpfRemoteChunkItemProcessor.required(
                parameters.getLong("fencingToken"), "fencingToken");

        long totalRead = 0;
        long totalWrite = 0;
        for (Map<String, Object> item : chunk) {
            fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
            BatchStepDefinition definition = CpfRemoteChunkItemProcessor.definition(item);
            BatchStepHandler handler = handlers.required(
                    definition.executorType(), definition.executorReference());
            Map<String, Object> jobParameters = new LinkedHashMap<>();
            parameters.getParameters().forEach(
                    (name, value) -> jobParameters.put(name, value.getValue()));
            BatchStepHandler.BatchStepResult result = handler.execute(
                    new BatchStepHandler.BatchStepCommand(
                            cpfExecutionId,
                            execution.getJobExecutionId(),
                            execution.getId(),
                            fencingToken,
                            definition,
                            jobParameters,
                            execution.getExecutionContext().toMap()));
            fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
            requireCompleted(result);
            totalRead += result.readCount();
            totalWrite += result.writeCount();
            execution.getExecutionContext().put("cpf.remote.chunk.lastCheckpoint", result.checkpoint());
            execution.getExecutionContext().putString("cpf.remote.chunk.lastResultCode", result.code());
        }
        execution.getExecutionContext().putLong("cpf.remote.chunk.readCount", totalRead);
        execution.getExecutionContext().putLong("cpf.remote.chunk.writeCount", totalWrite);
    }

    private static void requireCompleted(BatchStepHandler.BatchStepResult result) {
        if (result.status() == BatchStepHandler.Status.UNKNOWN_RESULT) {
            throw new CpfBatchUnknownResultException(result.code(), result.message());
        }
        if (result.status() == BatchStepHandler.Status.RETRYABLE_FAILURE) {
            throw new CpfBatchRetryableException(result.code(), result.message());
        }
        if (result.status() != BatchStepHandler.Status.COMPLETED) {
            throw new CpfBatchExecutionException(result.code(), result.message());
        }
    }
}
