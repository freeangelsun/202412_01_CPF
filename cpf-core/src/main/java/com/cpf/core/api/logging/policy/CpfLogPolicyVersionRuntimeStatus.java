package com.cpf.core.api.logging.policy;

import java.time.Instant;

/** Bounded runtime metrics for the versioned log-policy control plane. */
public record CpfLogPolicyVersionRuntimeStatus(
        Health health,
        int targetCount,
        int versionCount,
        int commandCount,
        int maximumTargets,
        int maximumHistoryPerTarget,
        int maximumCommandRecords,
        long rejectedCommandCount,
        long unknownResultCount,
        long auditFailureCount,
        long applyFailureCount,
        Instant observedAt) {
    public CpfLogPolicyVersionRuntimeStatus {
        if (health == null || observedAt == null) throw new IllegalArgumentException("health and observedAt are required");
        if (targetCount < 0 || versionCount < 0 || commandCount < 0
                || maximumTargets < 1 || maximumHistoryPerTarget < 2 || maximumCommandRecords < 16
                || targetCount > maximumTargets || rejectedCommandCount < 0L
                || unknownResultCount < 0L || auditFailureCount < 0L || applyFailureCount < 0L) {
            throw new IllegalArgumentException("invalid log-policy runtime status");
        }
    }
    public enum Health { UP, DEGRADED, DOWN }
}
