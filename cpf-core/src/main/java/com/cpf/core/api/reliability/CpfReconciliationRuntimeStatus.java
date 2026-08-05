package com.cpf.core.api.reliability;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** Bounded circuit-state monitoring surface for UNKNOWN reconciliation workers. */
public interface CpfReconciliationRuntimeStatus {
    RuntimeSnapshot reconciliationRuntimeSnapshot();

    enum Health { UP, DEGRADED, CAPACITY_EXHAUSTED }

    record RuntimeSnapshot(
            Health health,
            int circuitEntries,
            int maximumCircuitEntries,
            Duration circuitIdleTtl,
            long circuitEvictionCount,
            long circuitCapacityRejectionCount,
            Instant lastCircuitCapacityRejectionAt,
            Instant observedAt) {
        public RuntimeSnapshot {
            health = Objects.requireNonNull(health, "health");
            circuitIdleTtl = Objects.requireNonNull(circuitIdleTtl, "circuitIdleTtl");
            observedAt = Objects.requireNonNull(observedAt, "observedAt");
            if (circuitEntries < 0 || maximumCircuitEntries < 1
                    || circuitEntries > maximumCircuitEntries
                    || circuitEvictionCount < 0L || circuitCapacityRejectionCount < 0L) {
                throw new IllegalArgumentException("invalid reconciliation runtime counters");
            }
            if (circuitIdleTtl.isZero() || circuitIdleTtl.isNegative()) {
                throw new IllegalArgumentException("circuitIdleTtl must be positive");
            }
        }
    }
}
