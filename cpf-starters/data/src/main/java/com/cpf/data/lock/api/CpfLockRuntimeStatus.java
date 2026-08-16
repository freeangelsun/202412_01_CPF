package com.cpf.data.lock.api;

import java.time.Instant;

/** Lock Provider의 readiness/capacity/recovery 운영 상태를 노출하는 provider-neutral 계약. */
public interface CpfLockRuntimeStatus {
    LockRuntimeSnapshot lockRuntimeSnapshot(int limit);

    enum Health { UP, DEGRADED, CAPACITY_EXHAUSTED, DOWN }

    record LockRuntimeSnapshot(
            CpfLockManager.QueryStatus status,
            int total,
            int active,
            int expired,
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
        public LockRuntimeSnapshot {
            if (status == null || total < 0 || active < 0 || expired < 0 || released < 0 || forceReleased < 0
                    || highestFencingToken < 0 || trackedKeyCount < 0 || maximumTrackedKeys < 0
                    || capacityRejectionCount < 0 || observedAt == null || health == null) {
                throw new IllegalArgumentException("invalid lock runtime snapshot");
            }
        }

        /** LockRuntimeSnapshot 작업을 CPF 표준 계약에 따라 수행한다. */
        public LockRuntimeSnapshot(
                CpfLockManager.QueryStatus status,
                int total,
                int active,
                int expired,
                int released,
                int forceReleased,
                long highestFencingToken,
                Instant observedAt,
                Health health,
                String reason) {
            this(status, total, active, expired, released, forceReleased, highestFencingToken,
                    0, 0, 0L, null, observedAt, health, reason);
        }
    }
}
