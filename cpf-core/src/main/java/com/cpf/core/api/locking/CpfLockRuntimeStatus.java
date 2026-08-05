package com.cpf.core.api.locking;

import java.time.Instant;
import java.util.Objects;

/** Read-only monitoring surface for lock ownership, expiry, recovery and bounded-store health. */
public interface CpfLockRuntimeStatus {
    LockRuntimeSnapshot lockRuntimeSnapshot(int limit);

    enum Health { UP, DEGRADED, CAPACITY_EXHAUSTED, DOWN, UNKNOWN }

    record LockRuntimeSnapshot(
            CpfLockManager.QueryStatus queryStatus,
            int scanned,
            int active,
            int expiredButUnreconciled,
            int released,
            int forceReleased,
            long highestFencingToken,
            int trackedKeyCount,
            int maximumTrackedKeys,
            long capacityRejectionCount,
            Instant lastCapacityRejectionAt,
            Instant observedAt,
            Health health,
            String reason) {
        /** Source-compatible constructor for providers compiled before capacity visibility. */
        public LockRuntimeSnapshot(
                CpfLockManager.QueryStatus queryStatus,
                int scanned,
                int active,
                int expiredButUnreconciled,
                int released,
                int forceReleased,
                long highestFencingToken,
                Instant observedAt,
                Health health,
                String reason) {
            this(queryStatus, scanned, active, expiredButUnreconciled, released, forceReleased,
                    highestFencingToken, 0, 0, 0L, null, observedAt, health, reason);
        }

        public LockRuntimeSnapshot {
            queryStatus = Objects.requireNonNull(queryStatus, "queryStatus");
            observedAt = Objects.requireNonNull(observedAt, "observedAt");
            health = Objects.requireNonNull(health, "health");
            if (scanned < 0 || active < 0 || expiredButUnreconciled < 0 || released < 0
                    || forceReleased < 0 || highestFencingToken < 0 || trackedKeyCount < 0
                    || maximumTrackedKeys < 0 || capacityRejectionCount < 0L) {
                throw new IllegalArgumentException("lock monitoring counters must be non-negative");
            }
            if (maximumTrackedKeys > 0 && trackedKeyCount > maximumTrackedKeys) {
                throw new IllegalArgumentException("trackedKeyCount exceeds maximumTrackedKeys");
            }
            reason = reason == null ? "" : reason;
        }
    }
}
