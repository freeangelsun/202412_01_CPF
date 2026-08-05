package com.cpf.core.spi.locking;

import com.cpf.core.api.locking.CpfLockManager;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

/** Atomic persistence SPI. Implementations serialize and compare-and-set transitions per key. */
public interface CpfLockStore {
    UpdateResult update(String key, UnaryOperator<StoredLock> transition);
    Optional<StoredLock> find(String key);
    List<StoredLock> list(int limit);
    long nextFence(String key);

    /** Bounded providers expose their cardinality so readiness consumers can distinguish exhaustion. */
    default CapacitySnapshot capacitySnapshot() {
        return CapacitySnapshot.unbounded();
    }

    record CapacitySnapshot(
            boolean bounded,
            int trackedKeyCount,
            int maximumTrackedKeys,
            long capacityRejectionCount,
            Instant lastCapacityRejectionAt) {
        public CapacitySnapshot {
            if (trackedKeyCount < 0 || maximumTrackedKeys < 0 || capacityRejectionCount < 0L) {
                throw new IllegalArgumentException("lock capacity counters must be non-negative");
            }
            if (bounded && (maximumTrackedKeys < 1 || trackedKeyCount > maximumTrackedKeys)) {
                throw new IllegalArgumentException("invalid bounded lock capacity snapshot");
            }
            if (!bounded && maximumTrackedKeys != 0) {
                throw new IllegalArgumentException("unbounded lock capacity must use maximumTrackedKeys=0");
            }
        }

        public static CapacitySnapshot unbounded() {
            return new CapacitySnapshot(false, 0, 0, 0L, null);
        }
    }

    /** Typed fail-closed signal for providers that cannot safely retain another fencing history. */
    final class ResourceExhaustedException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public ResourceExhaustedException(String message) {
            super(message);
        }
    }

    record StoredLock(
            String key,
            String ownerId,
            String requestId,
            long fencingToken,
            long ownerEpoch,
            long rowVersion,
            Instant acquiredAt,
            Instant leaseUntil,
            CpfLockManager.State state,
            String lastReason,
            String lastAuditId) {
        public StoredLock {
            if (key == null || key.isBlank() || fencingToken < 1 || ownerEpoch < 1 || rowVersion < 1
                    || acquiredAt == null || leaseUntil == null || state == null) {
                throw new IllegalArgumentException("complete persisted lock state is required");
            }
        }

        /** Source-compatible constructor for the original fencing-only persistence contract. */
        public StoredLock(
                String key,
                String ownerId,
                String requestId,
                long fencingToken,
                Instant acquiredAt,
                Instant leaseUntil,
                CpfLockManager.State state,
                String lastReason,
                String lastAuditId) {
            this(key, ownerId, requestId, fencingToken, fencingToken, 1L,
                    acquiredAt, leaseUntil, state, lastReason, lastAuditId);
        }
    }

    record UpdateResult(StoredLock before, StoredLock after) {}
}
