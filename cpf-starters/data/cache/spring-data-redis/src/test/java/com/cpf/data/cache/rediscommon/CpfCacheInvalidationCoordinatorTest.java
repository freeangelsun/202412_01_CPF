package com.cpf.data.cache.rediscommon;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cpf.data.cache.api.CpfCacheHealth;
import com.cpf.data.cache.api.CpfCacheInvalidationEvent;
import com.cpf.data.cache.api.CpfCacheInvalidationPort;
import com.cpf.data.cache.api.CpfCacheKey;
import com.cpf.data.cache.api.CpfCacheMetricsSnapshot;
import com.cpf.data.cache.api.CpfCache;
import com.cpf.data.cache.api.CpfCacheValue;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Durable-first Cache invalidation의 중복·역순·재조정 의미를 Owner Module에서 검증합니다. */
class CpfCacheInvalidationCoordinatorTest {
    @Test
    void duplicateAndOutOfOrderVersionsDoNotReapply() {
        FakeCache cache = new FakeCache();
        FakeLedger ledger = new FakeLedger();
        CpfCacheInvalidationProperties properties = new CpfCacheInvalidationProperties();
        properties.setConsumerId("cache-test-01");
        CpfCacheInvalidationCoordinator coordinator =
                new CpfCacheInvalidationCoordinator(cache, ledger, ignored -> { }, properties);

        coordinator.request("op-1", new CpfCacheKey("member", "42", "tenant-a"), 2, "change", "tester");
        coordinator.request("op-2", new CpfCacheKey("member", "42", "tenant-a"), 1, "late", "tester");

        assertEquals(1, cache.evictions);
        assertEquals(2, ledger.checkpoint("cache-test-01"));
        assertEquals(2, ledger.version("cache-test-01", "tenant-a", "member", "42"));
    }

    @Test
    void reconcileReplaysDurableEventsAfterCheckpoint() {
        FakeCache cache = new FakeCache();
        FakeLedger ledger = new FakeLedger();
        ledger.append(new CpfCacheInvalidationEvent(0, "op-r1", "tenant-a", "member", "99", 3,
                "recovery", "tester", Instant.now()));
        CpfCacheInvalidationProperties properties = new CpfCacheInvalidationProperties();
        properties.setConsumerId("cache-test-02");
        CpfCacheInvalidationCoordinator coordinator =
                new CpfCacheInvalidationCoordinator(cache, ledger, null, properties);

        assertEquals(1, coordinator.reconcileNow());
        assertEquals(1, cache.evictions);
        assertEquals(1, ledger.checkpoint("cache-test-02"));
    }

    private static final class FakeCache implements CpfCache {
        int evictions;
        @Override public CpfCacheValue get(CpfCacheKey key) { return CpfCacheValue.miss(); }
        @Override public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) { }
        @Override public boolean evict(CpfCacheKey key) { evictions++; return true; }
        @Override public long evictNamespace(String tenantId, String namespace) { evictions++; return 1; }
        @Override public CpfCacheMetricsSnapshot metrics() { return new CpfCacheMetricsSnapshot("TEST", 0, 0, 0, evictions, 0, 0, 0, Instant.now()); }
        @Override public CpfCacheHealth health() { return new CpfCacheHealth(true, "TEST", "LOCAL", false, true, System.currentTimeMillis(), List.of(), Instant.now()); }
    }

    private static final class FakeLedger implements CpfCacheInvalidationPort {
        private final List<CpfCacheInvalidationEvent> events = new ArrayList<>();
        private final java.util.Map<String, Long> checkpoints = new java.util.HashMap<>();
        private final java.util.Map<String, Long> versions = new java.util.HashMap<>();

        @Override public CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event) {
            CpfCacheInvalidationEvent persisted = new CpfCacheInvalidationEvent(events.size() + 1L,
                    event.eventKey(), event.tenantId(), event.namespace(), event.cacheKey(), event.version(),
                    event.reason(), event.requestedBy(), event.createdAt());
            events.add(persisted); return persisted;
        }
        @Override public List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit) {
            return events.stream().filter(e -> e.eventId() > checkpoint).limit(limit).toList();
        }
        @Override public long checkpoint(String consumerId) { return checkpoints.getOrDefault(consumerId, 0L); }
        @Override public void checkpoint(String consumerId, long eventId) { checkpoints.merge(consumerId, eventId, (left, right) -> Math.max(left, right)); }
        @Override public long backlog(String consumerId) { return Math.max(0, events.size() - checkpoint(consumerId)); }
        @Override public long version(String consumerId, String tenantId, String namespace, String cacheKey) {
            return versions.getOrDefault(subject(consumerId, tenantId, namespace, cacheKey), 0L);
        }
        @Override public void advanceVersion(String consumerId, String tenantId, String namespace, String cacheKey, long version) {
            versions.merge(subject(consumerId, tenantId, namespace, cacheKey), version, (left, right) -> Math.max(left, right));
        }
        private String subject(String c, String t, String n, String k) { return c + "|" + t + "|" + n + "|" + k; }
    }
}
