package com.cpf.integration.realtime;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CpfRealtimeBrokerTest {
    @Test
    void duplicateReplayMultiInstanceAndDrain() throws Exception {
        CpfRealtimeProperties p = properties();
        CpfLocalRealtimeBackplane backplane = new CpfLocalRealtimeBackplane();
        try (CpfRealtimeBroker a = new CpfRealtimeBroker("a", p, backplane);
             CpfRealtimeBroker b = new CpfRealtimeBroker("b", p, backplane)) {
            List<CpfRealtimeBroker.Delivery> deliveries = new CopyOnWriteArrayList<>();
            CpfRealtimeBroker.Subscription sub = b.subscribe(filter(), "", deliveries::add);
            CpfRealtimeEvent input = event("evt-1", "one");
            CpfRealtimeEvent first = a.publish(input);
            CpfRealtimeEvent duplicate = a.publish(input);
            assertNotNull(first);
            assertEquals(first.sequence(), duplicate.sequence());
            await(() -> deliveries.stream().filter(CpfRealtimeBroker.Delivery.Event.class::isInstance).count() == 1);
            assertEquals(1, b.poll(filter(), "", 10).size());
            sub.close();
            b.drain();
            assertFalse(b.isAccepting());
            assertThrows(CpfRealtimeBroker.RealtimeUnavailableException.class, () -> b.poll(filter(), "", 10));
        }
    }

    @Test
    void replayAndConnectionLimitAreFailClosed() {
        CpfRealtimeProperties p = properties();
        p.setMaxConnectionsPerTenant(1);
        try (CpfRealtimeBroker broker = new CpfRealtimeBroker("one", p, new CpfLocalRealtimeBackplane())) {
            broker.publish(event("evt-1", "one"));
            broker.publish(event("evt-2", "two"));
            List<CpfRealtimeBroker.Delivery> replay = new CopyOnWriteArrayList<>();
            CpfRealtimeBroker.Subscription first = broker.subscribe(filter(), "evt-1", replay::add);
            assertThrows(CpfRealtimeBroker.RealtimeLimitException.class, () -> broker.subscribe(filter(), "", ignored -> {}));
            first.close();
        }
    }

    @Test
    void slowConsumerIsDisconnectedInsteadOfUnboundedGrowth() throws Exception {
        CpfRealtimeProperties p = properties();
        p.setSubscriberQueueCapacity(1);
        try (CpfRealtimeBroker broker = new CpfRealtimeBroker("one", p, new CpfLocalRealtimeBackplane())) {
            CpfRealtimeBroker.Subscription sub = broker.subscribe(filter(), "", delivery -> {
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
            for (int i = 0; i < 20; i++) broker.publish(event("evt-" + i, Integer.toString(i)));
            await(() -> !sub.closeReason().isEmpty());
            assertTrue(sub.closeReason().contains("backpressure") || sub.closeReason().contains("consumer"));
        }
    }

    private static CpfRealtimeProperties properties() {
        CpfRealtimeProperties p = new CpfRealtimeProperties();
        p.setReplayCapacity(32); p.setSubscriberQueueCapacity(8); p.setMaxConnectionsPerTenant(4);
        p.setMaxSubscribeAttemptsPerSecond(100); p.setHeartbeatInterval(Duration.ofHours(1)); p.setDrainTimeout(Duration.ofMillis(200));
        return p;
    }
    private static CpfRealtimeBroker.Filter filter() { return new CpfRealtimeBroker.Filter("t1", "ADM", "health", ""); }
    private static CpfRealtimeEvent event(String id, String payload) {
        return new CpfRealtimeEvent(0, id, "ADM", "health", "t1", "system", "TX", payload, Instant.now());
    }
    private static void await(Check check) throws Exception {
        long end = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
        while (System.nanoTime() < end) { if (check.ok()) return; Thread.sleep(10); }
        fail("condition not reached");
    }
    private interface Check { boolean ok(); }
}
