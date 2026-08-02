package com.cpf.common.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.core.api.cache.CpfCacheHealth;
import com.cpf.core.api.cache.CpfCacheInvalidationEvent;
import com.cpf.core.api.cache.CpfCacheInvalidationPort;
import com.cpf.core.api.cache.CpfCacheKey;
import com.cpf.core.api.cache.CpfCacheMetricsSnapshot;
import com.cpf.core.api.cache.CpfCachePort;
import com.cpf.core.api.cache.CpfCacheValue;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CpfCacheInvalidationCoordinatorTest {

    @Test
    void namespaceRequestIsDurableAndAppliedImmediately() {
        FakeCache cache = new FakeCache();
        FakeDurable durable = new FakeDurable();
        CpfRedisProperties properties = new CpfRedisProperties();
        properties.setConsumerId("cache-test-a");
        CpfCacheInvalidationCoordinator coordinator =
                new CpfCacheInvalidationCoordinator(cache, durable, null, properties);

        CpfCacheInvalidationEvent event = coordinator.requestNamespace(
                "op-1", "TENANT_A", "reference", 7, "운영 무효화", "operator-1");

        assertThat(event.eventId()).isEqualTo(1);
        assertThat(event.cacheKey()).isEmpty();
        assertThat(cache.namespaceEvictions).containsExactly("TENANT_A:reference");
        assertThat(durable.events).containsExactly(event);
    }

    @Test
    void reconcileUsesTheCoordinatorConsumerCheckpointAndDoesNotReplayAckedEvents() {
        FakeCache cache = new FakeCache();
        FakeDurable durable = new FakeDurable();
        durable.append(new CpfCacheInvalidationEvent(
                0, "op-1", "GLOBAL", "code", "A", 1, "test", "tester", Instant.now()));
        durable.append(new CpfCacheInvalidationEvent(
                0, "op-2", "GLOBAL", "message", "", 2, "test", "tester", Instant.now()));
        CpfRedisProperties properties = new CpfRedisProperties();
        properties.setConsumerId("cache-test-b");
        CpfCacheInvalidationCoordinator coordinator =
                new CpfCacheInvalidationCoordinator(cache, durable, null, properties);

        assertThat(coordinator.reconcileNow()).isEqualTo(2);
        assertThat(durable.checkpoint(coordinator.consumerId())).isEqualTo(2);
        assertThat(coordinator.reconcileNow()).isZero();
        assertThat(cache.keyEvictions).containsExactly("cpf:GLOBAL:code:A");
        assertThat(cache.namespaceEvictions).containsExactly("GLOBAL:message");
    }

    private static final class FakeDurable implements CpfCacheInvalidationPort {
        private final AtomicLong sequence = new AtomicLong();
        private final List<CpfCacheInvalidationEvent> events = new ArrayList<>();
        private final Map<String, Long> checkpoints = new HashMap<>();

        @Override
        public CpfCacheInvalidationEvent append(CpfCacheInvalidationEvent event) {
            CpfCacheInvalidationEvent persisted = new CpfCacheInvalidationEvent(
                    sequence.incrementAndGet(), event.eventKey(), event.tenantId(), event.namespace(),
                    event.cacheKey(), event.version(), event.reason(), event.requestedBy(), event.createdAt());
            events.add(persisted);
            return persisted;
        }

        @Override
        public List<CpfCacheInvalidationEvent> loadAfter(long checkpoint, int limit) {
            return events.stream().filter(event -> event.eventId() > checkpoint).limit(limit).toList();
        }

        @Override
        public long checkpoint(String consumerId) { return checkpoints.getOrDefault(consumerId, 0L); }

        @Override
        public void checkpoint(String consumerId, long eventId) { checkpoints.put(consumerId, eventId); }

        @Override
        public long backlog(String consumerId) {
            long checkpoint = checkpoint(consumerId);
            return events.stream().filter(event -> event.eventId() > checkpoint).count();
        }
    }

    private static final class FakeCache implements CpfCachePort {
        private final List<String> keyEvictions = new ArrayList<>();
        private final List<String> namespaceEvictions = new ArrayList<>();

        @Override

        public CpfCacheValue get(CpfCacheKey key) { return null; }
        @Override
        public void put(CpfCacheKey key, CpfCacheValue value, Duration ttl) { }
        @Override
        public boolean evict(CpfCacheKey key) { keyEvictions.add(key.canonical()); return true; }
        @Override
        public long evictNamespace(String tenantId, String namespace) {
            namespaceEvictions.add(tenantId + ":" + namespace);
            return 1;
        }
        @Override
        public CpfCacheMetricsSnapshot metrics() {
            return new CpfCacheMetricsSnapshot("FAKE", 0, 0, 0, 0, 0, 0, 0, Instant.now());
        }
        @Override
        public CpfCacheHealth health() {
            return new CpfCacheHealth(true, "FAKE", "LOCAL", false, true, 0, List.of(), Instant.now());
        }
    }
}
