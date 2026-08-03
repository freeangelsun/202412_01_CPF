package com.cpf.starter.data.cache.caffeine;

import com.cpf.core.api.cache.*;
import com.github.benmanes.caffeine.cache.Cache;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

/** Caffeine를 CPF L1 Cache Primary Engine으로 사용하는 실제 Adapter입니다. */
public final class CaffeineCpfCachePort implements CpfCachePort {
    private final Cache<CpfCacheKey, Entry> cache;
    private final long maximumPayloadBytes;
    private final LongAdder hits = new LongAdder();
    private final LongAdder misses = new LongAdder();
    private final LongAdder puts = new LongAdder();
    private final LongAdder evictions = new LongAdder();
    private final LongAdder errors = new LongAdder();

    public CaffeineCpfCachePort(Cache<CpfCacheKey, Entry> cache, long maximumPayloadBytes) {
        this.cache = cache;
        this.maximumPayloadBytes = maximumPayloadBytes;
    }

    @Override
    public CpfCacheValue get(CpfCacheKey key) {
        try {
            Entry entry = cache.getIfPresent(key);
            if (entry == null || entry.expiresAt().isBefore(Instant.now())) {
                if (entry != null) cache.invalidate(key);
                misses.increment();
                return CpfCacheValue.miss();
            }
            hits.increment();
            return entry.value();
        } catch (RuntimeException e) {
            errors.increment();
            throw e;
        }
    }

    @Override
    public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) {
        if (ttl == null || ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("ttl must be positive.");
        if (value.payload().length > maximumPayloadBytes) throw new IllegalArgumentException("cache payload exceeds configured limit.");
        cache.put(key, new Entry(value, Instant.now().plus(ttl)));
        puts.increment();
    }

    @Override public boolean evict(CpfCacheKey key) { boolean existed = cache.asMap().remove(key) != null; if (existed) evictions.increment(); return existed; }
    @Override public long evictNamespace(String tenantId, String namespace) {
        long count = cache.asMap().keySet().stream().filter(k -> k.tenantId().equals(tenantId) && k.namespace().equalsIgnoreCase(namespace))
                .peek(cache::invalidate).count();
        evictions.add(count); return count;
    }
    @Override public CpfCacheMetricsSnapshot metrics() {
        return new CpfCacheMetricsSnapshot("CAFFEINE", hits.sum(), misses.sum(), puts.sum(), evictions.sum(), errors.sum(), 0, 0, Instant.now());
    }
    @Override public CpfCacheHealth health() {
        return new CpfCacheHealth(true, "CAFFEINE", "LOCAL_L1", false, false, System.currentTimeMillis(), List.of(), Instant.now());
    }
    public record Entry(CpfCacheValue value, Instant expiresAt) { }
}
