package com.cpf.core.api.cache;

import java.time.Instant;

/** ADM이 Provider별 상태를 비교할 수 있는 누적 Cache 지표입니다. */
public record CpfCacheMetricsSnapshot(String provider, long hits, long misses, long puts, long evictions,
                                      long errors, long lockContentions, long invalidationLag, Instant observedAt) {
    public CpfCacheMetricsSnapshot {
        provider = provider == null || provider.isBlank() ? "UNKNOWN" : provider;
        observedAt = observedAt == null ? Instant.now() : observedAt;
        if (hits < 0 || misses < 0 || puts < 0 || evictions < 0 || errors < 0 || lockContentions < 0 || invalidationLag < 0) {
            throw new IllegalArgumentException("Cache 누적 지표는 음수일 수 없습니다.");
        }
    }
}
