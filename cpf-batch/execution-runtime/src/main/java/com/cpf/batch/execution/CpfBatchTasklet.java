package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/** 모든 File/Shell/API/Message Product Consumer를 Spring Batch StepExecution 안에서 호출합니다. */
public final class CpfBatchTasklet implements Tasklet {
    private final BatchStepDefinition definition;
    private final CpfBatchStepHandlerRegistry registry;
    private final BatchFencingPort fencing;

    public CpfBatchTasklet(
            BatchStepDefinition definition,
            CpfBatchStepHandlerRegistry registry,
            BatchFencingPort fencing) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.fencing = Objects.requireNonNull(fencing, "fencing");
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        StepExecution stepExecution = contribution.getStepExecution();
        JobParameters parameters = stepExecution.getJobExecution().getJobParameters();
        String cpfExecutionId = parameters.getString("cpfExecutionId");
        long fencingToken = requiredLong(parameters, "fencingToken");
        String jobId = parameters.getString("jobId");
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);

        Map<String, Object> jobParameters = new LinkedHashMap<>();
        parameters.getParameters().forEach((name, parameter) -> jobParameters.put(name, parameter.getValue()));
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        Map<String, Object> checkpoint = new LinkedHashMap<>(executionContext.toMap());

        BatchStepHandler handler = registry.required(definition.executorType(), definition.executorReference());
        BatchStepHandler.BatchStepResult result = handler.execute(new BatchStepHandler.BatchStepCommand(
                cpfExecutionId,
                stepExecution.getJobExecutionId(),
                stepExecution.getId(),
                fencingToken,
                definition,
                jobParameters,
                checkpoint));

        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
        result.checkpoint().forEach(executionContext::put);
        executionContext.putString("cpf.result.code", result.code());
        executionContext.putString("cpf.result.message", result.message());
        executionContext.putLong("cpf.read.count", result.readCount());
        executionContext.putLong("cpf.write.count", result.writeCount());
        executionContext.putLong("cpf.skip.count", result.skipCount());
        stepExecution.setReadCount(stepExecution.getReadCount() + result.readCount());
        contribution.incrementWriteCount(result.writeCount());
        contribution.incrementWriteSkipCount(result.skipCount());

        return switch (result.status()) {
            case COMPLETED -> RepeatStatus.FINISHED;
            case STOPPED -> throw new CpfBatchStoppedException(result.code(), result.message());
            case RETRYABLE_FAILURE -> throw new CpfBatchRetryableException(result.code(), result.message());
            case UNKNOWN_RESULT -> throw new CpfBatchUnknownResultException(result.code(), result.message());
            case FAILED -> throw new CpfBatchExecutionException(result.code(), result.message());
        };
    }

    private static long requiredLong(JobParameters parameters, String name) {
        Long value = parameters.getLong(name);
        if (value == null || value <= 0) throw new IllegalStateException(name + " is missing");
        return value;
    }
}
