package com.cpf.batch.api;

import java.util.Map;
import java.util.Objects;

/** CPF Control Plane이 승인하는 Step 정의이며 실행 상태는 Spring Batch StepExecution이 정본입니다. */
public record BatchStepDefinition(
        String stepId,
        BatchJobDefinition.ExecutorType executorType,
        String executorReference,
        Map<String, Object> parameters,
        int partitionCount,
        String nextOnSuccess,
        String nextOnFailure,
        boolean restartable) {
    public BatchStepDefinition {
        if (stepId == null || !stepId.matches("[A-Za-z0-9._-]{1,100}")) {
            throw new IllegalArgumentException("Invalid stepId.");
        }
        executorType = Objects.requireNonNull(executorType, "executorType");
        if (executorReference == null || executorReference.isBlank() || executorReference.length() > 512) {
            throw new IllegalArgumentException("executorReference is required and must not exceed 512 characters.");
        }
        executorReference = executorReference.trim();
        parameters = BatchCanonicalDigest.immutableParameters(parameters);
        if (partitionCount <= 0 || partitionCount > 10_000) {
            throw new IllegalArgumentException("partitionCount must be between 1 and 10000.");
        }
        nextOnSuccess = clean(nextOnSuccess);
        nextOnFailure = clean(nextOnFailure);
    }

    private static String clean(String value) {
        String cleaned = value == null ? "" : value.trim();
        if (cleaned.length() > 100) throw new IllegalArgumentException("Step transition exceeds 100 characters.");
        return cleaned;
    }
}
