package com.cpf.common.cache;

import com.cpf.core.api.cache.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * DB 원장을 정본으로 사용하고 Redis Pub/Sub를 빠른 전달 채널로 사용하는 재조정 Coordinator입니다.
 */
public final class CpfCacheInvalidationCoordinator {
    private final CpfCachePort cache;
    private final CpfCacheInvalidationPort durable;
    private final StringRedisTemplate redis;
    private final CpfRedisProperties properties;
    private final String consumerId;
    private final AtomicBoolean reconciling = new AtomicBoolean();

    public CpfCacheInvalidationCoordinator(CpfCachePort cache, CpfCacheInvalidationPort durable,
            StringRedisTemplate redis, CpfRedisProperties properties) {
        this.cache = cache;
        this.durable = durable;
        this.redis = redis;
        this.properties = properties;
        this.consumerId = properties.getConsumerId().isBlank()
                ? "cache-" + UUID.randomUUID() : properties.getConsumerId();
    }

    public CpfCacheInvalidationEvent request(String operationId, CpfCacheKey key, long version,
                                              String reason, String requestedBy) {
        CpfCacheInvalidationEvent event = durable.append(new CpfCacheInvalidationEvent(0, operationId,
                key.tenantId(), key.namespace(), key.key(), version, reason, requestedBy, null));
        apply(event);
        publishFastSignal(event);
        return event;
    }

    private void publishFastSignal(CpfCacheInvalidationEvent event) {
        if (redis == null) return;
        try { redis.convertAndSend(properties.getInvalidationChannel(), Long.toString(event.eventId())); }
        catch (RuntimeException ignored) { /* Durable 원장이 있으므로 업무 Transaction을 오염시키지 않습니다. */ }
    }

    /** Namespace 전체 무효화를 Durable 원장에 기록하고 현재 Instance에 즉시 적용합니다. */
    public CpfCacheInvalidationEvent requestNamespace(String operationId, String tenantId, String namespace,
                                                       long version, String reason, String requestedBy) {
        CpfCacheKey validated = new CpfCacheKey(namespace, "_namespace_", tenantId);
        CpfCacheInvalidationEvent event = durable.append(new CpfCacheInvalidationEvent(0, operationId,
                validated.tenantId(), validated.namespace(), "", version, reason, requestedBy, null));
        apply(event);
        publishFastSignal(event);
        return event;
    }

    /** 이 Coordinator가 사용하는 Durable checkpoint Consumer ID입니다. */
    public String consumerId() { return consumerId; }

    @Scheduled(fixedDelayString = "${cpf.cache.reconcile-delay:PT5S}")
    public void reconcile() { reconcileNow(); }

    /** 운영 API가 즉시 호출할 수 있는 단일 Batch 재조정입니다. 이미 실행 중이면 0을 반환합니다. */
    public int reconcileNow() {
        if (!reconciling.compareAndSet(false, true)) return 0;
        int appliedCount = 0;
        try {
            long checkpoint = durable.checkpoint(consumerId);
            var events = durable.loadAfter(checkpoint, properties.getReconcileBatchSize());
            for (CpfCacheInvalidationEvent event : events) {
                apply(event);
                durable.checkpoint(consumerId, event.eventId());
                appliedCount++;
            }
            return appliedCount;
        } finally {
            reconciling.set(false);
        }
    }

    public void onFastSignal(String ignoredEventId) { reconcileNow(); }

    private void apply(CpfCacheInvalidationEvent event) {
        if (event.cacheKey().isBlank()) {
            cache.evictNamespace(event.tenantId(), event.namespace());
        } else {
            cache.evict(new CpfCacheKey(event.namespace(), event.cacheKey(), event.tenantId()));
        }
    }
}
