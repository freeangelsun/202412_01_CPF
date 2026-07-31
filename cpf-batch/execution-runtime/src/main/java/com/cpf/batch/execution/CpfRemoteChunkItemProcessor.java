package com.cpf.batch.execution;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.StepSynchronizationManager;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ItemProcessor;

/** Remote Chunk Worker에서 CPF 업무 Handler를 Spring Batch StepExecution 안에서 실행합니다. */
public final class CpfRemoteChunkItemProcessor implements ItemProcessor<Map<String, Object>, Map<String, Object>> {
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;

    public CpfRemoteChunkItemProcessor(CpfBatchStepHandlerRegistry handlers, BatchFencingPort fencing) {
        this.handlers = handlers;
        this.fencing = fencing;
    }

    @Override
    public Map<String, Object> process(Map<String, Object> item) throws Exception {
        var context = StepSynchronizationManager.getContext();
        if (context == null) throw new IllegalStateException("BATCH_REMOTE_CHUNK_STEP_CONTEXT_MISSING");
        StepExecution execution = context.getStepExecution();
        JobParameters parameters = execution.getJobExecution().getJobParameters();
        String cpfExecutionId = required(parameters.getString("cpfExecutionId"), "cpfExecutionId");
        String jobId = required(parameters.getString("jobId"), "jobId");
        long fencingToken = required(parameters.getLong("fencingToken"), "fencingToken");
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);

        BatchStepDefinition definition = definition(item);
        BatchStepHandler handler = handlers.required(definition.executorType(), definition.executorReference());
        Map<String, Object> jobParameters = new LinkedHashMap<>();
        parameters.getParameters().forEach((name, value) -> jobParameters.put(name, value.getValue()));
        BatchStepHandler.BatchStepResult result = handler.execute(new BatchStepHandler.BatchStepCommand(
                cpfExecutionId,
                execution.getJobExecutionId(),
                execution.getId(),
                fencingToken,
                definition,
                jobParameters,
                execution.getExecutionContext().toMap()));
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
        if (result.status() == BatchStepHandler.Status.UNKNOWN_RESULT) {
            throw new CpfBatchUnknownResultException(result.code(), result.message());
        }
        if (result.status() == BatchStepHandler.Status.RETRYABLE_FAILURE) {
            throw new CpfBatchRetryableException(result.code(), result.message());
        }
        if (result.status() != BatchStepHandler.Status.COMPLETED) {
            throw new CpfBatchExecutionException(result.code(), result.message());
        }
        Map<String, Object> output = new LinkedHashMap<>(item);
        output.put("resultCode", result.code());
        output.put("resultMessage", result.message());
        output.put("readCount", result.readCount());
        output.put("writeCount", result.writeCount());
        output.put("checkpoint", result.checkpoint());
        return Map.copyOf(output);
    }

    @SuppressWarnings("unchecked")
    private static BatchStepDefinition definition(Map<String, Object> item) {
        Object raw = item.get("parameters");
        Map<String, Object> stepParameters = raw instanceof Map<?, ?> map
                ? map.entrySet().stream().collect(java.util.stream.Collectors.toMap(
                        entry -> String.valueOf(entry.getKey()), Map.Entry::getValue,
                        (left, right) -> right, LinkedHashMap::new))
                : Map.of();
        return new BatchStepDefinition(
                required(String.valueOf(item.get("stepId")), "stepId"),
                BatchJobDefinition.ExecutorType.valueOf(required(String.valueOf(item.get("executorType")), "executorType")),
                required(String.valueOf(item.get("executorReference")), "executorReference"),
                stepParameters,
                number(item.get("partitionCount"), 1),
                "", "",
                !Boolean.FALSE.equals(item.get("restartable")));
    }

    private static int number(Object value, int fallback) {
        return value instanceof Number number ? number.intValue() : fallback;
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank() || "null".equals(value)) throw new IllegalStateException(name + " is missing");
        return value;
    }
    private static long required(Long value, String name) {
        if (value == null || value <= 0) throw new IllegalStateException(name + " is missing");
        return value;
    }
}
