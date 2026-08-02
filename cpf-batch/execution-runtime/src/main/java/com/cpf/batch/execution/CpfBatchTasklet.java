package com.cpf.batch.execution;

import com.cpf.batch.api.BatchCanonicalDigest;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/** 모든 File/Shell/API/Message Product Consumer를 Spring Batch StepExecution 안에서 호출합니다. */
public final class CpfBatchTasklet implements Tasklet {
    private static final int MAX_CHECKPOINT_ENTRIES = 128;
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
        String cpfExecutionId = required(parameters, "cpfExecutionId");
        long fencingToken = requiredLong(parameters, "fencingToken");
        String jobId = required(parameters, "jobId");

        Map<String, Object> jobParameters = new LinkedHashMap<>();
        parameters.forEach(parameter -> jobParameters.put(parameter.name(), parameter.value()));
        ExecutionContext executionContext = stepExecution.getExecutionContext();
        Map<String, Object> checkpoint = new LinkedHashMap<>(executionContext.toMap());
        BatchStepHandler handler = registry.required(definition.executorType(), definition.executorReference());

        int maxAttempts = boundedInt(definition.parameters().get("retryMaxAttempts"), 1, 1, 10);
        long backoffMillis = boundedLong(definition.parameters().get("retryBackoffMillis"), 0L, 0L, 60_000L);
        BatchStepHandler.BatchStepResult result = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
            result = handler.execute(new BatchStepHandler.BatchStepCommand(
                    cpfExecutionId,
                    stepExecution.getJobExecutionId(),
                    stepExecution.getId(),
                    fencingToken,
                    definition,
                    jobParameters,
                    checkpoint));
            fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
            if (result.status() != BatchStepHandler.Status.RETRYABLE_FAILURE || attempt == maxAttempts) break;
            executionContext.putInt("cpf.retry.attempt", attempt);
            if (backoffMillis > 0) {
                try {
                    Thread.sleep(backoffMillis);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new CpfBatchStoppedException("BATCH_RETRY_INTERRUPTED", "Retry interrupted");
                }
            }
        }
        if (result == null) throw new IllegalStateException("Batch step handler returned no result");

        writeCheckpoint(executionContext, result.checkpoint());
        executionContext.putString("cpf.result.code", boundedText(result.code(), 100));
        executionContext.putString("cpf.result.message", boundedText(result.message(), 1_000));
        executionContext.putLong("cpf.read.count", result.readCount());
        executionContext.putLong("cpf.write.count", result.writeCount());
        executionContext.putLong("cpf.skip.count", result.skipCount());
        stepExecution.setReadCount(stepExecution.getReadCount() + result.readCount());
        contribution.incrementWriteCount(result.writeCount());
        contribution.incrementWriteSkipCount(result.skipCount());

        return switch (result.status()) {
            case COMPLETED -> RepeatStatus.FINISHED;
            case STOPPED -> {
                contribution.setExitStatus(new ExitStatus("STOPPED", boundedText(result.message(), 1_000)));
                stepExecution.setTerminateOnly();
                yield RepeatStatus.FINISHED;
            }
            case RETRYABLE_FAILURE -> throw new CpfBatchRetryableException(result.code(), result.message());
            case UNKNOWN_RESULT -> throw new CpfBatchUnknownResultException(result.code(), result.message());
            case FAILED -> throw new CpfBatchExecutionException(result.code(), result.message());
        };
    }

    private static void writeCheckpoint(ExecutionContext context, Map<String, Object> values) {
        Map<String, Object> safe = BatchCanonicalDigest.immutableParameters(values);
        if (safe.size() > MAX_CHECKPOINT_ENTRIES) {
            throw new IllegalArgumentException("Checkpoint entry count exceeds " + MAX_CHECKPOINT_ENTRIES);
        }
        for (Map.Entry<String, Object> entry : safe.entrySet()) {
            String key = entry.getKey();
            if (!key.matches("[A-Za-z0-9._-]{1,120}")) {
                throw new IllegalArgumentException("Invalid checkpoint key: " + key);
            }
            Object value = entry.getValue();
            if (value instanceof String text) context.putString(key, boundedText(text, 4_000));
            else if (value instanceof Integer number) context.putInt(key, number);
            else if (value instanceof Long number) context.putLong(key, number);
            else if (value instanceof Double number) context.putDouble(key, number);
            else if (value instanceof Boolean flag) context.putString(key, flag.toString());
            else context.putString(key, BatchCanonicalDigest.canonicalText(value));
        }
    }

    private static String required(JobParameters parameters, String name) {
        String value = parameters.getString(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is missing");
        return value;
    }

    private static long requiredLong(JobParameters parameters, String name) {
        Long value = parameters.getLong(name);
        if (value == null || value <= 0) throw new IllegalStateException(name + " is missing");
        return value;
    }

    private static int boundedInt(Object value, int defaultValue, int min, int max) {
        int result = value == null ? defaultValue : Integer.parseInt(value.toString());
        if (result < min || result > max) throw new IllegalArgumentException("Retry attempts out of range");
        return result;
    }

    private static long boundedLong(Object value, long defaultValue, long min, long max) {
        long result = value == null ? defaultValue : Long.parseLong(value.toString());
        if (result < min || result > max) throw new IllegalArgumentException("Retry backoff out of range");
        return result;
    }

    private static String boundedText(String value, int maximum) {
        String clean = value == null ? "" : value
                .replaceAll("(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>")
                .replaceAll("[\\r\\n\\t]+", " ").trim();
        return clean.length() <= maximum ? clean : clean.substring(0, maximum);
    }
}
