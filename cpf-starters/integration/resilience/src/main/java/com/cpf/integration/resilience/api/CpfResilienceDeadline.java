package com.cpf.integration.resilience.api;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/** Monotonic deadline shared by the engine, queue and transport adapters. */
/** CpfResilienceDeadline 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfResilienceDeadline {
    private final CpfResilienceRuntimePolicy policy;
    private final LongSupplier nanoTime;
    private final long deadlineNanos;

    public CpfResilienceDeadline(CpfResilienceRuntimePolicy policy, LongSupplier nanoTime) {
        this(policy, nanoTime, Objects.requireNonNull(policy, "policy").overallTimeout());
    }

    /** CpfResilienceDeadline 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfResilienceDeadline(
            CpfResilienceRuntimePolicy policy, LongSupplier nanoTime, Duration initialRemaining) {
        this.policy = Objects.requireNonNull(policy, "policy");
        this.nanoTime = Objects.requireNonNull(nanoTime, "nanoTime");
        Duration bounded = nonNegative(initialRemaining, "initialRemaining");
        if (bounded.compareTo(policy.overallTimeout()) > 0) bounded = policy.overallTimeout();
        this.deadlineNanos = saturatingAdd(nanoTime.getAsLong(), bounded.toNanos());
    }

    /** remaining 작업을 CPF 표준 계약에 따라 수행한다. */
    public Duration remaining() {
        long remaining = deadlineNanos - nanoTime.getAsLong();
        return remaining <= 0L ? Duration.ZERO : Duration.ofNanos(remaining);
    }

    public Duration remainingFor(CpfResilienceRuntimePolicy.Stage stage) {
        Duration remaining = remaining();
        Duration stageLimit = policy.timeoutFor(stage);
        return remaining.compareTo(stageLimit) < 0 ? remaining : stageLimit;
    }

    /** cap 작업을 CPF 표준 계약에 따라 수행한다. */
    public Duration cap(Duration requested) {
        Duration value = nonNegative(requested, "requested");
        Duration remaining = remaining();
        return remaining.compareTo(value) < 0 ? remaining : value;
    }

    public boolean expired() {
        return remaining().isZero();
    }

    private static Duration nonNegative(Duration value, String name) {
        if (value == null || value.isNegative()) throw new IllegalArgumentException(name + " must be non-negative");
        return value;
    }

    private static long saturatingAdd(long left, long right) {
        long result = left + right;
        if (((left ^ result) & (right ^ result)) < 0) return Long.MAX_VALUE;
        return result;
    }
}
