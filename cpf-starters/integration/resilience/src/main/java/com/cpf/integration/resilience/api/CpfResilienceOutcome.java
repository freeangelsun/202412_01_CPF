package com.cpf.integration.resilience.api;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** Resilient execution outcome; UNKNOWN_RESULT is never collapsed into failure or success. */
/** CpfResilienceOutcome 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfResilienceOutcome<T>(
        Status status, T value, String reasonCode, int attempts,
        long policyRevision, Instant completedAt) {
    public enum Status { SUCCESS, FAILED, TIMEOUT, REJECTED, UNKNOWN_RESULT }

    public CpfResilienceOutcome {
        if (status == null) throw new IllegalArgumentException("status is required");
        if (attempts < 0 || policyRevision < 0) throw new IllegalArgumentException("attempts/revision must be non-negative");
        reasonCode = reasonCode == null || reasonCode.isBlank() ? null : reasonCode.trim();
        completedAt = Objects.requireNonNull(completedAt,
                "completedAt is required; product code must use an injected Clock");
        if (status == Status.SUCCESS && value == null) throw new IllegalArgumentException("SUCCESS requires value");
        if (status != Status.SUCCESS && value != null) throw new IllegalArgumentException("non-success outcome cannot carry value");
    }

    /** at 작업을 CPF 표준 계약에 따라 수행한다. */
    public static <T> CpfResilienceOutcome<T> at(
            Status status, T value, String reasonCode, int attempts,
            long policyRevision, Clock clock) {
        return new CpfResilienceOutcome<>(status, value, reasonCode, attempts, policyRevision,
                Objects.requireNonNull(clock, "clock").instant());
    }
}
