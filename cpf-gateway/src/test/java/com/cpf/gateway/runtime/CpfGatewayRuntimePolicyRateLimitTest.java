package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRateLimitCounterPort;
import com.cpf.core.api.gateway.CpfGatewayRateLimitPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class CpfGatewayRuntimePolicyRateLimitTest {
    private static final Instant NOW = Instant.parse("2026-08-05T00:00:05Z");

    @Test
    void appliesQuotaBurstRetryAfterAndStrictestScope() {
        var policy = localPolicy();
        policy.replaceRates(1L,
                new CpfGatewayRuntimePolicy.Limit(2, 60_000L, 1, 0, 0L),
                Map.of(),
                Map.of("client", new CpfGatewayRuntimePolicy.Limit(1, 60_000L)),
                Map.of(), Map.of(), true);
        assertTrue(decision(policy, "one").allowed());
        var denied = decision(policy, "two");
        assertFalse(denied.allowed());
        assertEquals(CpfGatewayRateLimitPort.Scope.CLIENT, denied.limitingScope());
        assertEquals(55L, denied.retryAfter().toSeconds());
        assertEquals("QUOTA_EXCEEDED", denied.reason());
    }

    @Test
    void duplicateGatewayRequestDoesNotConsumeAgain() {
        var policy = localPolicy();
        policy.replaceRates(1L, new CpfGatewayRuntimePolicy.Limit(1, 60_000L), Map.of());
        assertTrue(decision(policy, "server-tx").allowed());
        var duplicate = decision(policy, "server-tx");
        assertTrue(duplicate.allowed());
        assertTrue(duplicate.duplicate());
        assertFalse(decision(policy, "other-tx").allowed());
    }

    @Test
    void concurrentRequestsNeverExceedQuota() throws Exception {
        var policy = localPolicy();
        policy.replaceRates(1L, new CpfGatewayRuntimePolicy.Limit(100, 60_000L), Map.of());
        int tasks = 500;
        AtomicInteger allowed = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(32)) {
            for (int i = 0; i < tasks; i++) {
                int id = i;
                executor.submit(() -> {
                    try {
                        start.await();
                        if (decision(policy, "request-" + id).allowed()) allowed.incrementAndGet();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            start.countDown();
            executor.shutdown();
            assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS));
        }
        assertEquals(100, allowed.get());
    }

    @Test
    void counterFailureFailsClosedUnlessExplicitlyConfiguredOtherwise() {
        CpfGatewayRateLimitCounterPort failing = new CpfGatewayRateLimitCounterPort() {
            @Override public CounterResult consume(CounterCommand command) { throw new IllegalStateException("down"); }
            @Override public CounterHealth health() { throw new IllegalStateException("down"); }
            @Override public boolean distributed() { return true; }
        };
        var failClosed = new CpfGatewayRuntimePolicy(failing, true, fixed());
        failClosed.replaceRates(1L, new CpfGatewayRuntimePolicy.Limit(1, 60_000L), Map.of());
        var denied = decision(failClosed, "closed");
        assertFalse(denied.allowed());
        assertTrue(denied.degraded());
        assertEquals("COUNTER_UNAVAILABLE", denied.reason());

        var failOpen = new CpfGatewayRuntimePolicy(failing, false, fixed());
        failOpen.replaceRates(1L, new CpfGatewayRuntimePolicy.Limit(1, 60_000L), Map.of());
        var allowed = decision(failOpen, "open");
        assertTrue(allowed.allowed());
        assertTrue(allowed.degraded());
        assertEquals("ALLOWED_DEGRADED", allowed.reason());
    }


    @Test
    void rejectsStaleAndSameVersionDifferentPayloadButAllowsIdempotentReplay() {
        var policy = localPolicy();
        var limit = new CpfGatewayRuntimePolicy.Limit(10, 60_000L);
        var first = policy.replaceRates(5L, limit, Map.of());
        assertSame(first, policy.replaceRates(5L, limit, Map.of()));
        assertThrows(IllegalStateException.class,
                () -> policy.replaceRates(5L, new CpfGatewayRuntimePolicy.Limit(11, 60_000L), Map.of()));
        assertThrows(IllegalStateException.class,
                () -> policy.replaceRates(4L, limit, Map.of()));
    }

    @Test
    void exposesOnlyOpaquePolicyIdentifiersAndSanitizedStatusCounts() {
        var policy = localPolicy();
        policy.replaceRates(9L,
                CpfGatewayRuntimePolicy.Limit.unlimited(), Map.of(),
                Map.of("customer@example.com", new CpfGatewayRuntimePolicy.Limit(1, 60_000L)),
                Map.of(), Map.of(), true);
        var decision = policy.acquire(new CpfGatewayRateLimitPort.Request(
                "api", "route", "customer@example.com", "", "", "opaque", 1, NOW));
        assertTrue(decision.allowed());
        assertFalse(decision.policyId().contains("customer@example.com"));
        var status = policy.status();
        assertEquals(9L, status.version());
        assertEquals(1, status.clientPolicies());
        assertTrue(status.failClosedOnCounterFailure());
    }
    @Test
    void malformedProviderResultFailsClosedEvenWhenOutagePolicyIsFailOpen() {
        CpfGatewayRateLimitCounterPort malformed = new CpfGatewayRateLimitCounterPort() {
            @Override
            public CounterResult consume(CounterCommand command) {
                return new CounterResult(false, false, 0L, 0L,
                        command.resetAtEpochMillis(), 0L, 0, "BROKEN");
            }

            @Override
            public BatchResult consumeAtomically(java.util.List<CounterCommand> commands) {
                return new BatchResult(true, -1, java.util.List.of(consume(commands.getFirst())));
            }

            @Override
            public CounterHealth health() {
                return new CounterHealth(true, 1L, "UP", NOW);
            }

            @Override
            public boolean distributed() {
                return true;
            }
        };
        CpfGatewayRuntimePolicy policy = new CpfGatewayRuntimePolicy(malformed, false, fixed());
        policy.replaceRates(99L, new CpfGatewayRuntimePolicy.Limit(10, 60_000L), Map.of());

        var decision = decision(policy, "malformed");

        assertFalse(decision.allowed());
        assertTrue(decision.degraded());
        assertEquals("COUNTER_CONTRACT_INVALID", decision.reason());
    }


    @Test
    void mixedDuplicateStateFromProviderFailsClosed() {
        CpfGatewayRateLimitCounterPort malformed = new CpfGatewayRateLimitCounterPort() {
            @Override public CounterResult consume(CounterCommand command) {
                throw new UnsupportedOperationException();
            }
            @Override public BatchResult consumeAtomically(java.util.List<CounterCommand> commands) {
                CounterCommand first = commands.get(0);
                CounterCommand second = commands.get(1);
                return new BatchResult(true, -1, java.util.List.of(
                        new CounterResult(true, true, 1L, 9L,
                                first.resetAtEpochMillis(), 0L, 0, "ALLOWED"),
                        new CounterResult(true, false, 1L, 9L,
                                second.resetAtEpochMillis(), 0L, 0, "ALLOWED")));
            }
            @Override public CounterHealth health() {
                return new CounterHealth(true, 2L, "UP", NOW);
            }
            @Override public boolean distributed() { return true; }
        };
        var policy = new CpfGatewayRuntimePolicy(malformed, true, fixed());
        policy.replaceRates(100L, CpfGatewayRuntimePolicy.Limit.unlimited(), Map.of(),
                Map.of("client", new CpfGatewayRuntimePolicy.Limit(10, 60_000L)),
                Map.of("channel", new CpfGatewayRuntimePolicy.Limit(10, 60_000L)),
                Map.of(), true);

        var decision = decision(policy, "mixed-duplicate");

        assertFalse(decision.allowed());
        assertTrue(decision.degraded());
        assertEquals("COUNTER_CONTRACT_INVALID", decision.reason());
    }

    @Test
    void rejectsOversizedSubjectsBlankPolicyKeysAndNegativeProviderResults() {
        assertThrows(IllegalArgumentException.class, () -> new CpfGatewayRateLimitPort.Request(
                "api", "route", "x".repeat(201), "", "", "request", 1, NOW));
        assertThrows(IllegalArgumentException.class, () -> new CpfGatewayRateLimitCounterPort.CounterResult(
                true, false, -1L, 0L, NOW.toEpochMilli(), 0L, 0, "ALLOWED"));

        var policy = localPolicy();
        assertThrows(IllegalArgumentException.class, () -> policy.replaceRates(
                1L, CpfGatewayRuntimePolicy.Limit.unlimited(),
                java.util.Collections.singletonMap(" ", new CpfGatewayRuntimePolicy.Limit(1, 60_000L)),
                Map.of(), Map.of(), Map.of(), true));
    }

    private static CpfGatewayRuntimePolicy localPolicy() {
        return new CpfGatewayRuntimePolicy(
                new InMemoryCpfGatewayRateLimitCounterAdapter(1_000, fixed()), true, fixed());
    }

    private static CpfGatewayRateLimitPort.Decision decision(
            CpfGatewayRuntimePolicy policy, String requestId) {
        return policy.acquire(new CpfGatewayRateLimitPort.Request(
                "api", "route", "client", "channel", "tenant", requestId, 1, NOW));
    }

    @Test
    void clientQuotaCannotBeBypassedByChangingExecutionRoute() {
        var policy = new CpfGatewayRuntimePolicy(
                new InMemoryCpfGatewayRateLimitCounterAdapter(100, fixed()), true, fixed());
        policy.replaceRates(1L, CpfGatewayRuntimePolicy.Limit.unlimited(), Map.of(),
                Map.of("client-a", new CpfGatewayRuntimePolicy.Limit(1, 60_000L)),
                Map.of(), Map.of(), true);

        var first = policy.acquire(new CpfGatewayRateLimitPort.Request(
                "OAPI000001", "route-a", "client-a", "", "", "request-a", 1, NOW));
        var second = policy.acquire(new CpfGatewayRateLimitPort.Request(
                "OAPI000002", "route-b", "client-a", "", "", "request-b", 1, NOW));

        assertTrue(first.allowed());
        assertFalse(second.allowed());
        assertEquals(CpfGatewayRateLimitPort.Scope.CLIENT, second.limitingScope());
    }

    private static Clock fixed() {
        return Clock.fixed(NOW, ZoneOffset.UTC);
    }
}
