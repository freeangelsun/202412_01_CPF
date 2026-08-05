package com.cpf.core.api.logging;

import java.time.Duration;
import java.time.Instant;

/** Bounded local log-policy cache metrics without exposing policy values or target identifiers. */
public interface CpfLogPolicyCacheRuntimeStatus {
    RuntimeSnapshot logPolicyCacheRuntimeSnapshot();

    record RuntimeSnapshot(
            Health health,
            int entryCount,
            int maximumEntries,
            Duration ttl,
            long hitCount,
            long missCount,
            long refreshCount,
            long evictionCount,
            long failureCount,
            Instant observedAt) {
        public RuntimeSnapshot {
            if (health == null || ttl == null || observedAt == null) {
                throw new IllegalArgumentException("health, ttl and observedAt are required");
            }
            if (entryCount < 0 || maximumEntries < 1 || entryCount > maximumEntries
                    || ttl.isZero() || ttl.isNegative() || hitCount < 0L || missCount < 0L
                    || refreshCount < 0L || evictionCount < 0L || failureCount < 0L) {
                throw new IllegalArgumentException("invalid log-policy cache snapshot");
            }
        }
    }

    enum Health { UP, DEGRADED }
}
