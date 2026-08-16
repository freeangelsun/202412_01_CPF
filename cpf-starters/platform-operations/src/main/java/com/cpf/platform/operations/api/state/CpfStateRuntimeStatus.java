package com.cpf.platform.operations.api.state;

import java.time.Instant;

/** Operational health and bounded-resource projection for a state provider. */
/** CpfStateRuntimeStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfStateRuntimeStatus {
    RuntimeSnapshot stateRuntimeSnapshot();

    record RuntimeSnapshot(
            Health health,
            int stateCount,
            int maximumStates,
            long appliedCount,
            long replayCount,
            long versionConflictCount,
            long operationConflictCount,
            long resourceExhaustedCount,
            long providerFailureCount,
            Instant observedAt) {
        public RuntimeSnapshot {
            if (health == null || observedAt == null) throw new IllegalArgumentException("health and observedAt are required");
            if (stateCount < 0 || maximumStates < 1 || stateCount > maximumStates) {
                throw new IllegalArgumentException("invalid state capacity snapshot");
            }
        }
    }

    /** Health 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    enum Health { UP, DEGRADED, DOWN }
}
