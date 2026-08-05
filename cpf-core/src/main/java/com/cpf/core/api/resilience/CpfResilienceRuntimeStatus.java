package com.cpf.core.api.resilience;

import java.time.Duration;
import java.time.Instant;

/** Bounded-resource and lifecycle status for the resilience execution engine. */
public interface CpfResilienceRuntimeStatus {
    RuntimeSnapshot resilienceRuntimeSnapshot();

    record RuntimeSnapshot(
            Health health,
            int guardCount,
            int maximumGuardEntries,
            Duration guardIdleTtl,
            int activeAttemptCount,
            long guardEvictionCount,
            long guardCapacityRejectionCount,
            boolean closed,
            Instant observedAt) {
        public RuntimeSnapshot {
            if (health == null || guardIdleTtl == null || observedAt == null) {
                throw new IllegalArgumentException("health, guardIdleTtl and observedAt are required");
            }
            if (guardCount < 0 || maximumGuardEntries < 1 || guardCount > maximumGuardEntries
                    || activeAttemptCount < 0 || guardEvictionCount < 0L
                    || guardCapacityRejectionCount < 0L || guardIdleTtl.isNegative()
                    || guardIdleTtl.isZero()) {
                throw new IllegalArgumentException("invalid resilience runtime snapshot");
            }
        }
    }

    enum Health { UP, DEGRADED, DOWN }
}
