package com.cpf.common.cache;

import com.cpf.core.api.cache.CpfCacheInvalidationEvent;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import com.cpf.core.api.cache.CpfCacheKey;
import com.cpf.core.api.cache.CpfCachePort;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Durable-first cache invalidation coordinator.
 *
 * <p>The database ledger is authoritative. A fast signal only reduces propagation latency and may
 * fail without losing the event. A checkpoint advances only after the local cache operation
 * succeeds, so process termination and partial failure are recovered by {@link #reconcileNow()}.</p>
 */
public final class CpfCacheInvalidationCoordinator {
    @FunctionalInterface
    public interface FastSignalPublisher {
        void publish(String eventKey);
    }

    private final CpfCachePort cache;
    private final CpfCacheInvalidationPort durable;
    private final FastSignalPublisher fastSignals;
    private final CpfRedisProperties properties;

    public CpfCacheInvalidationCoordinator(
            CpfCachePort cache,
            CpfCacheInvalidationPort durable,
            FastSignalPublisher fastSignals,
            CpfRedisProperties properties) {
        this.cache = Objects.requireNonNull(cache, "cache");
        this.durable = Objects.requireNonNull(durable, "durable");
        this.fastSignals = fastSignals;
        this.properties = Objects.requireNonNull(properties, "properties");
        properties.validate();
    }

    public String consumerId() { return properties.getConsumerId(); }

    public CpfCacheInvalidationEvent request(
            String eventKey,
            CpfCacheKey key,
            long version,
            String reason,
            String requestedBy) {
        Objects.requireNonNull(key, "key");
        return persistApplyAndSignal(new CpfCacheInvalidationEvent(
                0, eventKey, key.tenantId(), key.namespace(), key.key(), version,
                reason, requestedBy, Instant.now()));
    }

    public CpfCacheInvalidationEvent requestNamespace(
            String eventKey,
            String tenantId,
            String namespace,
            long version,
            String reason,
            String requestedBy) {
        return persistApplyAndSignal(new CpfCacheInvalidationEvent(
                0, eventKey, tenantId, namespace, "", version,
                reason, requestedBy, Instant.now()));
    }

    /** Fast-channel callback. Payload is intentionally non-authoritative. */
    public int onFastSignal(String ignoredEventKey) {
        return reconcileNow();
    }

    public int reconcileNow() {
        int applied = 0;
        long checkpoint = durable.checkpoint(consumerId());
        for (int batch = 0; batch < properties.getReconcileMaxBatches(); batch++) {
            List<CpfCacheInvalidationEvent> events = durable.loadAfter(
                    checkpoint, properties.getReconcileBatchSize());
            if (events.isEmpty()) {
                break;
            }
            for (CpfCacheInvalidationEvent event : events) {
                apply(event);
                durable.checkpoint(consumerId(), event.eventId());
                checkpoint = event.eventId();
                applied++;
            }
            if (events.size() < properties.getReconcileBatchSize()) {
                break;
            }
        }
        return applied;
    }

    private CpfCacheInvalidationEvent persistApplyAndSignal(CpfCacheInvalidationEvent requested) {
        CpfCacheInvalidationEvent persisted = durable.append(requested);
        apply(persisted);
        durable.checkpoint(consumerId(), persisted.eventId());
        publishBestEffort(persisted.eventKey());
        return persisted;
    }

    private void apply(CpfCacheInvalidationEvent event) {
        if (event.cacheKey().isBlank()) {
            cache.evictNamespace(event.tenantId(), event.namespace());
        } else {
            cache.evict(new CpfCacheKey(event.namespace(), event.cacheKey(), event.tenantId()));
        }
    }

    private void publishBestEffort(String eventKey) {
        if (fastSignals == null) {
            return;
        }
        try {
            fastSignals.publish(eventKey);
        } catch (RuntimeException ignored) {
            // Durable ledger + reconciliation remain authoritative.
        }
    }
}
