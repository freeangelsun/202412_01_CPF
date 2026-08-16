package com.cpf.platform.operations.observability.api.logging.policy;

import java.time.Instant;

/** Bounded runtime metrics for the versioned log-policy control plane. */
/** CpfLogPolicyVersionRuntimeStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
    /** Health 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Health { UP, DEGRADED, DOWN }
}
