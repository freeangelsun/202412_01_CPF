package com.cpf.core.api.cache;

import java.time.Duration;

/** Redis/Caffeine/테스트 Fake에 공통으로 적용되는 Cache SPI입니다. */
public interface CpfCachePort {
    CpfCacheValue get(CpfCacheKey key);
    void put(CpfCacheKey key, CpfCacheValue value, Duration ttl);
    boolean evict(CpfCacheKey key);
    long evictNamespace(String tenantId, String namespace);
    CpfCacheMetricsSnapshot metrics();
    CpfCacheHealth health();
}
