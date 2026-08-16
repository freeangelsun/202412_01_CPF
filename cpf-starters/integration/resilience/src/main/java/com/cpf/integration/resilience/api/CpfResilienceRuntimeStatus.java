package com.cpf.integration.resilience.api;

import java.time.Duration;
import java.time.Instant;

/** Bounded-resource and lifecycle status for the resilience execution engine. */
/** CpfResilienceRuntimeStatus 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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

    /** Health 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    enum Health { UP, DEGRADED, DOWN }
}
