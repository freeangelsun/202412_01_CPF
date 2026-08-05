package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeApplyGuardTest {

    @Test
    void expiredDeliveryNeverInvokesApplier() {
        AtomicInteger calls = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = guard(100, 1, 1, 3, 100)) {
            CpfRuntimeApplyResult result = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.success("hash")), delivery(Instant.now().minusSeconds(1)));

            assertFalse(result.applied());
            assertEquals("DELIVERY_EXPIRED", result.errorCode());
            assertEquals(0, calls.get());
        }
    }

    @Test
    void retriesOnlyExplicitTransientFailureThenSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        AtomicInteger prepared = new AtomicInteger();
        AtomicInteger cleared = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = guard(2_000, 3, 1, 3, 100)) {
            CpfRuntimeApplyResult result = guard.execute(applier(calls, count -> count < 3
                            ? CpfRuntimeApplyResult.failure("DEPENDENCY_UNAVAILABLE", "temporary")
                            : CpfRuntimeApplyResult.success("actual-3")),
                    delivery(Instant.now().plusSeconds(5)), prepared::incrementAndGet, cleared::incrementAndGet);

            assertTrue(result.applied());
            assertEquals("actual-3", result.actualHash());
            assertEquals(3, calls.get());
            assertEquals(3, prepared.get());
            assertEquals(2, cleared.get());
        }
    }

    @Test
    void unknownResultIsNeverRetriedAndOpensCircuit() {
        AtomicInteger calls = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = guard(1_000, 3, 1, 1, 10_000)) {
            CpfRuntimeApplyResult first = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.unknown("UNKNOWN_SIDE_EFFECT", "unknown")),
                    delivery(Instant.now().plusSeconds(5)));
            CpfRuntimeApplyResult second = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.success("should-not-run")),
                    delivery(Instant.now().plusSeconds(5)));

            assertTrue(first.unknownResult());
            assertEquals(1, calls.get());
            assertEquals("APPLY_CIRCUIT_OPEN", second.errorCode());
        }
    }

    @Test
    void halfOpenProbeClosesCircuitAfterCooldown() {
        Instant base = Instant.parse("2026-08-04T00:00:00Z");
        MutableClock clock = new MutableClock(base);
        CpfRuntimeApplyGuard.Policy policy = new CpfRuntimeApplyGuard.Policy(
                1_000L, 1, 0L, 0L, 0, 1, 1, 100L);
        AtomicInteger calls = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = new CpfRuntimeApplyGuard(policy, clock, ignored -> { }, () -> 0.5d)) {
            CpfRuntimeApplyResult failed = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.unknown("UNKNOWN_SIDE_EFFECT", "unknown")),
                    delivery(base.plusSeconds(5)));
            CpfRuntimeApplyResult open = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.success("blocked")), delivery(base.plusSeconds(5)));
            clock.advanceMillis(101L);
            CpfRuntimeApplyResult probe = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.success("probe-ok")), delivery(base.plusSeconds(5)));
            CpfRuntimeApplyResult closed = guard.execute(applier(calls, ignored ->
                    CpfRuntimeApplyResult.success("closed-ok")), delivery(base.plusSeconds(5)));

            assertTrue(failed.unknownResult());
            assertEquals("APPLY_CIRCUIT_OPEN", open.errorCode());
            assertTrue(probe.applied());
            assertTrue(closed.applied());
            assertEquals(3, calls.get());
        }
    }

    @Test
    void timeoutReturnsUnknownWithoutAutomaticRetry() {
        AtomicInteger calls = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = guard(40, 3, 1, 3, 1_000)) {
            CpfRuntimeApplyResult result = guard.execute(applier(calls, ignored -> {
                try {
                    Thread.sleep(1_000L);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return CpfRuntimeApplyResult.success("late");
            }), delivery(Instant.now().plusSeconds(5)));

            assertTrue(result.unknownResult());
            assertEquals("APPLY_TIMEOUT_UNKNOWN", result.errorCode());
            assertEquals(1, calls.get());
        }
    }

    @Test
    void timedOutTaskThatIgnoresInterruptKeepsBulkheadPermitUntilItActuallyStops() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        try (CpfRuntimeApplyGuard guard = guard(40, 1, 1, 3, 1_000)) {
            CpfRuntimeApplyResult timedOut = guard.execute(applier(firstCalls, ignored -> {
                started.countDown();
                while (release.getCount() > 0) {
                    try {
                        release.await(20, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ignoredInterrupt) {
                        // 외부 provider가 interrupt를 무시하는 최악의 경계를 재현합니다.
                    }
                }
                return CpfRuntimeApplyResult.success("late");
            }), delivery(Instant.now().plusSeconds(5)));

            assertTrue(started.await(1, TimeUnit.SECONDS));
            assertTrue(timedOut.unknownResult());
            CpfRuntimeApplyResult blocked = guard.execute(applier(secondCalls, ignored ->
                    CpfRuntimeApplyResult.success("must-not-overlap")), delivery(Instant.now().plusSeconds(5)));
            assertEquals("APPLY_BULKHEAD_FULL", blocked.errorCode());
            assertEquals(0, secondCalls.get());
            release.countDown();
        }
    }

    @Test
    void bulkheadRejectsConcurrentApplyWithoutStartingSecondSideEffect() throws Exception {
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        AtomicReference<CpfRuntimeApplyResult> firstResult = new AtomicReference<>();
        try (CpfRuntimeApplyGuard guard = guard(2_000, 1, 1, 3, 1_000)) {
            Thread first = new Thread(() -> firstResult.set(guard.execute(applier(firstCalls, ignored -> {
                firstStarted.countDown();
                try {
                    releaseFirst.await(1, TimeUnit.SECONDS);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                }
                return CpfRuntimeApplyResult.success("first");
            }), delivery(Instant.now().plusSeconds(5)))));
            first.start();
            assertTrue(firstStarted.await(1, TimeUnit.SECONDS));

            CpfRuntimeApplyResult rejected = guard.execute(applier(secondCalls, ignored ->
                    CpfRuntimeApplyResult.success("second")), delivery(Instant.now().plusSeconds(5)));
            releaseFirst.countDown();
            first.join(2_000L);

            assertEquals("APPLY_BULKHEAD_FULL", rejected.errorCode());
            assertEquals(0, secondCalls.get());
            assertTrue(firstResult.get().applied());
        }
    }

    private static CpfRuntimeApplyGuard guard(
            long timeoutMillis,
            int attempts,
            int concurrency,
            int circuitThreshold,
            long circuitOpenMillis) {
        return new CpfRuntimeApplyGuard(new CpfRuntimeApplyGuard.Policy(
                timeoutMillis, attempts, 1L, 5L, 0, concurrency, circuitThreshold, circuitOpenMillis));
    }

    private static CpfRuntimeChangeApplier applier(
            AtomicInteger calls,
            java.util.function.IntFunction<CpfRuntimeApplyResult> action) {
        return new CpfRuntimeChangeApplier() {
            @Override
            public String changeType() {
                return "TEST";
            }

            @Override
            public boolean supportsIdempotentReplay() {
                return true;
            }

            @Override
            public CpfRuntimeApplyResult apply(CpfRuntimeDelivery delivery) {
                return action.apply(calls.incrementAndGet());
            }
        };
    }

    private static CpfRuntimeDelivery delivery(Instant expiresAt) {
        return new CpfRuntimeDelivery(
                "delivery-1", "change-1", "TEST", "instance-1", 1L, 1L,
                "request-hash", "payload-hash", 1, CpfRuntimePayload.empty(), 0, expiresAt);
    }
    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }


    @Test
    void safeFailureCleanupExceptionBecomesUnknownAndReleasesHalfOpenProbe() {
        Instant base = Instant.parse("2026-08-05T00:00:00Z");
        MutableClock clock = new MutableClock(base);
        var policy = new CpfRuntimeApplyGuard.Policy(1_000, 2, 0, 0, 0, 1, 1, 10);
        AtomicInteger calls = new AtomicInteger();
        try (var guard = new CpfRuntimeApplyGuard(policy, clock, millis -> { }, () -> 0.5d)) {
            CpfRuntimeApplyResult first = guard.execute(
                    applier(calls, ignored -> CpfRuntimeApplyResult.failure("TEMPORARY_FAILURE", "retry")),
                    delivery(base.plusSeconds(5)),
                    () -> { },
                    () -> { throw new IllegalStateException("journal cleanup failed"); });

            assertTrue(first.unknownResult());
            assertEquals("APPLY_SAFE_FAILURE_CLEANUP_UNKNOWN", first.errorCode());

            clock.advanceMillis(11);
            CpfRuntimeApplyResult probe = guard.execute(
                    applier(calls, ignored -> CpfRuntimeApplyResult.success("hash-ok")),
                    delivery(base.plusSeconds(5)));
            assertTrue(probe.applied());
        }
    }

    @Test
    void closedGuardRejectsNewWorkAndCloseIsIdempotent() {
        var guard = new CpfRuntimeApplyGuard(CpfRuntimeApplyGuard.Policy.defaults());
        guard.close();
        guard.close();
        AtomicInteger calls = new AtomicInteger();

        CpfRuntimeApplyResult result = guard.execute(
                applier(calls, ignored -> CpfRuntimeApplyResult.success("unexpected")),
                delivery(Instant.now().plusSeconds(5)));

        assertFalse(result.applied());
        assertFalse(result.unknownResult());
        assertEquals("APPLY_GUARD_CLOSED", result.errorCode());
        assertEquals(0, calls.get());
    }


    @Test
    void zeroBackoffAllowsImmediateBoundedRetry() {
        try (CpfRuntimeApplyGuard guard = new CpfRuntimeApplyGuard(new CpfRuntimeApplyGuard.Policy(
                1_000L, 2, 0L, 0L, 0, 1, 3, 1_000L))) {
            for (int iteration = 0; iteration < 50; iteration++) {
                AtomicInteger calls = new AtomicInteger();
                CpfRuntimeChangeApplier applier = applier(calls, attempt -> attempt == 1
                        ? CpfRuntimeApplyResult.failure("TRANSIENT_FAILURE", "retry")
                        : CpfRuntimeApplyResult.success("actual-hash"));

                CpfRuntimeApplyResult result = guard.execute(applier, delivery(Instant.now().plusSeconds(5)));

                assertTrue(result.applied(), "immediate retry iteration=" + iteration);
                assertEquals(2, calls.get(), "immediate retry call count iteration=" + iteration);
            }
        }
    }

}
