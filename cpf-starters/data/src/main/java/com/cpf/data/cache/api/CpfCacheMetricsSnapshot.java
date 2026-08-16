package com.cpf.data.cache.api;
import java.time.Instant;
/** CpfCacheMetricsSnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfCacheMetricsSnapshot(String provider, long hits, long misses, long puts, long evictions,
        long errors, long lockContentions, long reconnects, Instant observedAt) { }
