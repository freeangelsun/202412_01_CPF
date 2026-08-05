package com.cpf.core.api.state;

import java.time.Instant;

/** Operational health and bounded-resource projection for a state provider. */
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

    enum Health { UP, DEGRADED, DOWN }
}
