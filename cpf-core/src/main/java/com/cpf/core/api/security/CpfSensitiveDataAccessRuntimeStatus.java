package com.cpf.core.api.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/** 민감정보 원문 승인 Store의 bounded-resource 운영 상태 계약입니다. */
public interface CpfSensitiveDataAccessRuntimeStatus {
    RuntimeSnapshot snapshot();

    enum Health {
        HEALTHY,
        CAPACITY_EXHAUSTED
    }

    record RuntimeSnapshot(
            Health health,
            int grantCount,
            int terminalGrantCount,
            int maximumGrants,
            Duration terminalRetention,
            long evictionCount,
            long capacityRejectionCount,
            Instant lastCapacityRejectionAt,
            Instant observedAt) {
        public RuntimeSnapshot {
            health = Objects.requireNonNull(health, "health");
            terminalRetention = Objects.requireNonNull(terminalRetention, "terminalRetention");
            observedAt = Objects.requireNonNull(observedAt, "observedAt");
            if (grantCount < 0 || terminalGrantCount < 0 || terminalGrantCount > grantCount) {
                throw new IllegalArgumentException("invalid grant counts");
            }
            if (maximumGrants < 1 || grantCount > maximumGrants) {
                throw new IllegalArgumentException("invalid maximumGrants");
            }
            if (terminalRetention.isNegative() || terminalRetention.isZero()) {
                throw new IllegalArgumentException("terminalRetention must be positive");
            }
            if (evictionCount < 0L || capacityRejectionCount < 0L) {
                throw new IllegalArgumentException("counters must be non-negative");
            }
        }
    }
}
