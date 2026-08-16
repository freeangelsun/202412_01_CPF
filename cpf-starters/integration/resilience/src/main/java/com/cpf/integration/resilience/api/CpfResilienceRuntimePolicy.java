package com.cpf.integration.resilience.api;

import java.time.Duration;
import java.util.Objects;

/**
 * Runtime-only limits layered over the persisted, provider-neutral policy.
 * The existing database contract remains stable while adapters can configure
 * connect/read/write and engine attempt/overall deadlines independently.
 */
/** CpfResilienceRuntimePolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfResilienceRuntimePolicy(
        CpfResiliencePolicy basePolicy,
        Duration connectTimeout,
        Duration tlsTimeout,
        Duration writeTimeout,
        Duration responseHeaderTimeout,
        Duration readTimeout,
        Duration attemptTimeout,
        Duration overallTimeout,
        Duration initialRetryBackoff,
        Duration maxRetryBackoff,
        double jitterRatio,
        int retryBudgetCapacity,
        Duration retryBudgetWindow,
        int bulkheadQueueLimit,
        Duration bulkheadQueueWait) {

    public CpfResilienceRuntimePolicy {
        basePolicy = Objects.requireNonNull(basePolicy, "basePolicy");
        connectTimeout = positive(connectTimeout, "connectTimeout");
        tlsTimeout = positive(tlsTimeout, "tlsTimeout");
        writeTimeout = positive(writeTimeout, "writeTimeout");
        responseHeaderTimeout = positive(responseHeaderTimeout, "responseHeaderTimeout");
        readTimeout = positive(readTimeout, "readTimeout");
        attemptTimeout = positive(attemptTimeout, "attemptTimeout");
        overallTimeout = positive(overallTimeout, "overallTimeout");
        initialRetryBackoff = nonNegative(initialRetryBackoff, "initialRetryBackoff");
        maxRetryBackoff = nonNegative(maxRetryBackoff, "maxRetryBackoff");
        retryBudgetWindow = positive(retryBudgetWindow, "retryBudgetWindow");
        bulkheadQueueWait = nonNegative(bulkheadQueueWait, "bulkheadQueueWait");
        if (maxRetryBackoff.compareTo(initialRetryBackoff) < 0) {
            throw new IllegalArgumentException("maxRetryBackoff must be >= initialRetryBackoff");
        }
        if (jitterRatio < 0.0d || jitterRatio > 1.0d || Double.isNaN(jitterRatio)) {
            throw new IllegalArgumentException("jitterRatio must be between 0 and 1");
        }
        if (retryBudgetCapacity < 0 || bulkheadQueueLimit < 0) {
            throw new IllegalArgumentException("budget and queue limits must be non-negative");
        }
        if (overallTimeout.compareTo(attemptTimeout) < 0) {
            throw new IllegalArgumentException("overallTimeout must be >= attemptTimeout");
        }
    }

    /** Source-compatible constructor for the pre TLS/header-stage contract. */
    /** CpfResilienceRuntimePolicy 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfResilienceRuntimePolicy(
            CpfResiliencePolicy basePolicy,
            Duration connectTimeout,
            Duration readTimeout,
            Duration writeTimeout,
            Duration attemptTimeout,
            Duration overallTimeout,
            Duration initialRetryBackoff,
            Duration maxRetryBackoff,
            double jitterRatio,
            int retryBudgetCapacity,
            Duration retryBudgetWindow,
            int bulkheadQueueLimit,
            Duration bulkheadQueueWait) {
        this(basePolicy, connectTimeout, connectTimeout, writeTimeout, readTimeout, readTimeout,
                attemptTimeout, overallTimeout, initialRetryBackoff, maxRetryBackoff, jitterRatio,
                retryBudgetCapacity, retryBudgetWindow, bulkheadQueueLimit, bulkheadQueueWait);
    }

    /** legacyCompatible 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfResilienceRuntimePolicy legacyCompatible(CpfResiliencePolicy policy) {
        Objects.requireNonNull(policy, "policy");
        Duration perAttempt = policy.timeoutBudget();
        Duration totalBackoff = safeMultiply(policy.retryBackoff(), Math.max(0, policy.maxAttempts() - 1));
        Duration overall = safeAdd(safeMultiply(perAttempt, policy.maxAttempts()), totalBackoff);
        return new CpfResilienceRuntimePolicy(
                policy,
                perAttempt,
                perAttempt,
                perAttempt,
                perAttempt,
                perAttempt,
                perAttempt,
                overall,
                policy.retryBackoff(),
                max(policy.retryBackoff(), Duration.ofSeconds(30)),
                0.20d,
                safeCapacity(policy.maxAttempts(), policy.bulkheadMaxConcurrent()),
                policy.rateLimitWindow(),
                0,
                Duration.ZERO);
    }

    /** timeoutFor 작업을 CPF 표준 계약에 따라 수행한다. */
    public Duration timeoutFor(Stage stage) {
        return switch (Objects.requireNonNull(stage, "stage")) {
            case CONNECT -> connectTimeout;
            case TLS -> tlsTimeout;
            case WRITE -> writeTimeout;
            case RESPONSE_HEADER -> responseHeaderTimeout;
            case READ -> readTimeout;
            case ATTEMPT -> attemptTimeout;
            case OVERALL -> overallTimeout;
        };
    }

    /** Stage 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Stage { CONNECT, TLS, WRITE, RESPONSE_HEADER, READ, ATTEMPT, OVERALL }

    private static final Duration MAX_TIMEOUT = Duration.ofDays(365);

    private static Duration positive(Duration value, String name) {
        if (value == null || value.isZero() || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        if (value.compareTo(MAX_TIMEOUT) > 0) {
            throw new IllegalArgumentException(name + " exceeds the 365-day safety bound");
        }
        return value;
    }
    private static Duration nonNegative(Duration value, String name) {
        if (value == null || value.isNegative()) {
            throw new IllegalArgumentException(name + " must be non-negative");
        }
        return value;
    }
    private static int safeCapacity(int attempts, int bulkhead) {
        long capacity = (long) Math.max(0, attempts - 1) * Math.max(1, bulkhead);
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }
    private static Duration max(Duration first, Duration second) {
        return first.compareTo(second) >= 0 ? first : second;
    }
    private static Duration safeMultiply(Duration value, int multiplier) {
        try {
            return value.multipliedBy(multiplier);
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (ArithmeticException overflow) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }
    }
    private static Duration safeAdd(Duration first, Duration second) {
        try {
            return first.plus(second);
        } catch (ArithmeticException overflow) {
            return Duration.ofNanos(Long.MAX_VALUE);
        }
    }
}
