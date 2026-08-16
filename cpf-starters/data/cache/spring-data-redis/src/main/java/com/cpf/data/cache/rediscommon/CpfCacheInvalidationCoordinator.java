package com.cpf.data.cache.rediscommon;

import com.cpf.data.cache.api.CpfCacheInvalidationEvent;
import com.cpf.data.cache.api.CpfCacheInvalidationPort;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCachePort;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
    private final CpfCacheInvalidationProperties properties;
    private final ConcurrentHashMap<String, ReentrantLock> subjectLocks = new ConcurrentHashMap<>();

    public CpfCacheInvalidationCoordinator(
            CpfCachePort cache,
            CpfCacheInvalidationPort durable,
            FastSignalPublisher fastSignals,
            CpfCacheInvalidationProperties properties) {
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
                applyWithVersionFence(event);
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
        applyWithVersionFence(persisted);
        durable.checkpoint(consumerId(), persisted.eventId());
        publishBestEffort(persisted.eventKey());
        return persisted;
    }

    private boolean applyWithVersionFence(CpfCacheInvalidationEvent event) {
        String subject = versionSubject(event);
        ReentrantLock lock = subjectLocks.computeIfAbsent(subject, ignored -> new ReentrantLock());
        lock.lock();
        try {
            long current = durable.version(
                    consumerId(), event.tenantId(), event.namespace(), event.cacheKey());
            if (event.version() <= current) {
                return false;
            }
            if (event.cacheKey().isBlank()) {
                cache.evictNamespace(event.tenantId(), event.namespace());
            } else {
                cache.evict(new CpfCacheKey(event.namespace(), event.cacheKey(), event.tenantId()));
            }
            durable.advanceVersion(
                    consumerId(), event.tenantId(), event.namespace(), event.cacheKey(), event.version());
            return true;
        } finally {
            lock.unlock();
            if (!lock.hasQueuedThreads()) {
                subjectLocks.remove(subject, lock);
            }
        }
    }

    private static String versionSubject(CpfCacheInvalidationEvent event) {
        return event.tenantId() + "\u0000" + event.namespace() + "\u0000" + event.cacheKey();
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
