package com.cpf.core.api.resilience;

import java.time.Duration;

/** Immutable operation policy independent of Resilience4j/Spring Cloud types. */
public record CpfResiliencePolicy(
        String operationId,
        long revision,
        Duration timeoutBudget,
        int maxAttempts,
        Duration retryBackoff,
        int circuitFailureThreshold,
        Duration circuitOpenDuration,
        int bulkheadMaxConcurrent,
        int rateLimitPermits,
        Duration rateLimitWindow,
        boolean idempotent,
        boolean unknownResultReconcileEnabled) {
    public CpfResiliencePolicy {
        if (operationId == null || operationId.isBlank()) throw new IllegalArgumentException("operationId is required");
        operationId = operationId.trim();
        if (revision < 0) throw new IllegalArgumentException("revision must be non-negative");
        timeoutBudget = positive(timeoutBudget, "timeoutBudget");
        retryBackoff = nonNegative(retryBackoff, "retryBackoff");
        circuitOpenDuration = positive(circuitOpenDuration, "circuitOpenDuration");
        rateLimitWindow = positive(rateLimitWindow, "rateLimitWindow");
        if (maxAttempts < 1 || circuitFailureThreshold < 1 || bulkheadMaxConcurrent < 1 || rateLimitPermits < 1) {
            throw new IllegalArgumentException("numeric policy limits must be positive");
        }
        if (!idempotent && maxAttempts > 1) throw new IllegalArgumentException("non-idempotent operation cannot retry automatically");
    }
    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) throw new IllegalArgumentException(name + " must be positive");
        return value;
    }
    private static Duration nonNegative(Duration value, String name) {
        if (value == null || value.isNegative()) throw new IllegalArgumentException(name + " must be non-negative");
        return value;
    }
}
