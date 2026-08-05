package com.cpf.core.api.resilience;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** Resilient execution outcome; UNKNOWN_RESULT is never collapsed into failure or success. */
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

    public static <T> CpfResilienceOutcome<T> at(
            Status status, T value, String reasonCode, int attempts,
            long policyRevision, Clock clock) {
        return new CpfResilienceOutcome<>(status, value, reasonCode, attempts, policyRevision,
                Objects.requireNonNull(clock, "clock").instant());
    }
}
