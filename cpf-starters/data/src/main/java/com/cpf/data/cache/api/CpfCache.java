package com.cpf.data.cache.api;

import java.time.Duration;
import java.util.Optional;

/**
 * 업무 개발자가 사용하는 CPF Cache의 단일 Public Golden Path입니다.
 *
 * <p>Provider 구현(Redis/Valkey/Caffeine/Local)을 노출하지 않고 기본 CRUD와 cache-aside,
 * negative-cache, single-flight를 동일 계약으로 제공합니다. 분산 Lock을 제공하는 Provider는
 * 해당 Lock을 사용하고, 단일 JVM Provider는 JVM-local single-flight로 중복 원본 조회를 억제합니다.</p>
 *
 * <p>Cache 장애를 fail-open으로 사용할지는 {@link CpfCacheOptions}에서 명시하며,
 * 원본 Loader 실패는 Cache miss로 숨기지 않습니다.</p>
 */
public interface CpfCache {
    CpfCacheValue get(CpfCacheKey key);
    void put(CpfCacheKey key, CpfCacheValue value, Duration ttl);
    boolean evict(CpfCacheKey key);
    long evictNamespace(String tenantId, String namespace);
    CpfCacheMetricsSnapshot metrics();
    CpfCacheHealth health();

    /**
     * Cache hit이면 즉시 반환하고 miss이면 single-flight로 원본 Loader를 한 번 실행합니다.
     *
     * @param key Cache key
     * @param options TTL/negative-cache/single-flight/fail-open 정책
     * @param loader Canonical 원본 저장소 Loader
     * @return hit 또는 원본에서 적재한 값. 원본이 없고 negative-cache가 꺼져 있으면 miss
     */
    default CpfCacheValue getOrLoad(CpfCacheKey key, CpfCacheOptions options, CpfCacheLoader loader) {
        return CpfCacheLoadingSupport.getOrLoad(this, key, options, loader);
    }

    /**
     * {@link #getOrLoad(CpfCacheKey, CpfCacheOptions, CpfCacheLoader)} 결과에서 실제 양의 값을 Optional로 반환합니다.
     * miss와 negative-cache hit는 empty로 표현합니다.
     */
    default Optional<CpfCacheValue> getOrLoadOptional(
            CpfCacheKey key,
            CpfCacheOptions options,
            CpfCacheLoader loader) {
        CpfCacheValue value = getOrLoad(key, options, loader);
        return value.found() && !value.negative() ? Optional.of(value) : Optional.empty();
    }
}
