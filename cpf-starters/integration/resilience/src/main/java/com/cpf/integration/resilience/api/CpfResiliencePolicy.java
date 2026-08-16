package com.cpf.integration.resilience.api;

import java.time.Duration;

/** Resilience4j 또는 Spring Cloud 구현 타입에 의존하지 않는 CPF 불변 복원력 정책 계약입니다. */
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
        if (operationId.length() > 256) throw new IllegalArgumentException("operationId exceeds 256 characters");
        if (operationId.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("operationId contains control characters");
        }
        if (revision < 0) throw new IllegalArgumentException("revision must be non-negative");
        timeoutBudget = positive(timeoutBudget, "timeoutBudget");
        retryBackoff = nonNegative(retryBackoff, "retryBackoff");
        circuitOpenDuration = positive(circuitOpenDuration, "circuitOpenDuration");
        rateLimitWindow = positive(rateLimitWindow, "rateLimitWindow");
        if (maxAttempts < 1 || circuitFailureThreshold < 1 || bulkheadMaxConcurrent < 1 || rateLimitPermits < 1) {
            throw new IllegalArgumentException("numeric policy limits must be positive");
        }
        if (maxAttempts > MAX_NUMERIC_LIMIT || circuitFailureThreshold > MAX_NUMERIC_LIMIT
                || bulkheadMaxConcurrent > MAX_NUMERIC_LIMIT || rateLimitPermits > MAX_NUMERIC_LIMIT) {
            throw new IllegalArgumentException("numeric policy limits exceed the safety bound");
        }
        if (!idempotent && maxAttempts > 1) throw new IllegalArgumentException("non-idempotent operation cannot retry automatically");
    }
    private static final Duration MAX_DURATION = Duration.ofDays(365);
    private static final int MAX_NUMERIC_LIMIT = 1_000_000;

    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        if (value.compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException(name + " exceeds the 365-day safety bound");
        }
        return value;
    }
    private static Duration nonNegative(Duration value, String name) {
        if (value == null || value.isNegative()) throw new IllegalArgumentException(name + " must be non-negative");
        if (value.compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException(name + " exceeds the 365-day safety bound");
        }
        return value;
    }
}
