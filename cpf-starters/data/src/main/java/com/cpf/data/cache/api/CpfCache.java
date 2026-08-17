package com.cpf.data.cache.api;
import java.time.Duration;
/** CpfCache 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfCache {
    CpfCacheValue get(CpfCacheKey key);
    void put(CpfCacheKey key, CpfCacheValue value, Duration ttl);
    boolean evict(CpfCacheKey key);
    long evictNamespace(String tenantId, String namespace);
    CpfCacheMetricsSnapshot metrics();
    CpfCacheHealth health();
}
