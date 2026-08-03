package com.cpf.core.api.resilience;

import java.time.Instant;

/** Resilient execution outcome; UNKNOWN_RESULT is never collapsed into failure or success. */
public record CpfResilienceOutcome<T>(
        Status status, T value, String reasonCode, int attempts,
        long policyRevision, Instant completedAt) {
    public enum Status { SUCCESS, FAILED, TIMEOUT, REJECTED, UNKNOWN_RESULT }
    public CpfResilienceOutcome {
        if (status == null) throw new IllegalArgumentException("status is required");
        if (attempts < 0 || policyRevision < 0) throw new IllegalArgumentException("attempts/revision must be non-negative");
        reasonCode = reasonCode == null || reasonCode.isBlank() ? null : reasonCode.trim();
        completedAt = completedAt == null ? Instant.now() : completedAt;
        if (status == Status.SUCCESS && value == null) throw new IllegalArgumentException("SUCCESS requires value");
        if (status != Status.SUCCESS && value != null) throw new IllegalArgumentException("non-success outcome cannot carry value");
    }
}
