package com.cpf.core.common.reconciliation;

import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.internal.locking.DefaultCpfLockManager;
import com.cpf.core.internal.locking.InMemoryCpfLockStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Verifies that a worker losing its lease during a probe cannot commit a stale result. */
public final class CpfReconciliationFenceLossHarness {
    private CpfReconciliationFenceLossHarness() {}

    public static void main(String[] args) {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfLockManager manager = new DefaultCpfLockManager(new InMemoryCpfLockStore(), null, clock);
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(1, true, 1_000, 0, 1, 5, false, Set.of("PAYMENT"), 3, 2, 5_000);

        AtomicBoolean completed = new AtomicBoolean();
        AtomicInteger claims = new AtomicInteger();
        AtomicInteger staleResolutions = new AtomicInteger();
        AtomicInteger freshResolutions = new AtomicInteger();
        CpfUnknownResultRecord record = new CpfUnknownResultRecord(
                "u-fence", "PAYMENT", "CHECK_PENDING", "tx-fence", null, "external-fence",
                null, null, null, clock.instant(), null);

        CpfReconciliationWorkPort work = new CpfReconciliationWorkPort() {
            @Override
            public List<WorkItem> claim(String type, int threshold, int limit, String worker, int lease) {
                claims.incrementAndGet();
                return completed.get() ? List.of() : List.of(new WorkItem(record, 0, 1));
            }

            @Override
            public void defer(String unknownId, String workerId, Instant nextCheckAt, String nextAction) {
                throw new AssertionError("stale worker must not defer");
            }

            @Override
            public void markManualReview(String unknownId, String workerId, String nextAction) {
                throw new AssertionError("stale worker must not mark manual review");
            }
        };

        CpfReconciliationPort stalePort = new CountingPort(staleResolutions, completed);
        CpfReconciliationProbePort expiringProbe = new CpfReconciliationProbePort() {
            @Override public boolean supports(String unknownType) { return "PAYMENT".equals(unknownType); }
            @Override public ProbeResult probe(CpfUnknownResultRecord ignored) {
                clock.advance(Duration.ofSeconds(6));
                return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "access_token=stale-secret");
            }
        };
        new CpfReconciliationWorker(
                stalePort, work, policy, List.of(expiringProbe), "worker-stale", clock, manager).tick();
        if (staleResolutions.get() != 0 || completed.get()) {
            throw new AssertionError("expired fence committed a result");
        }

        CpfReconciliationPort freshPort = new CountingPort(freshResolutions, completed);
        CpfReconciliationProbePort freshProbe = new CpfReconciliationProbePort() {
            @Override public boolean supports(String unknownType) { return "PAYMENT".equals(unknownType); }
            @Override public ProbeResult probe(CpfUnknownResultRecord ignored) {
                return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "confirmed");
            }
        };
        new CpfReconciliationWorker(
                freshPort, work, policy, List.of(freshProbe), "worker-fresh", clock, manager).tick();
        if (freshResolutions.get() != 1 || !completed.get()) {
            throw new AssertionError("fresh fenced worker did not reconcile");
        }
        if (claims.get() < 2) throw new AssertionError("expected stale and fresh claims");
        System.out.println("CPF_RECONCILIATION_FENCE_LOSS_HARNESS_PASS");
    }

    private static final class CountingPort implements CpfReconciliationPort {
        private final AtomicInteger resolutions;
        private final AtomicBoolean completed;

        private CountingPort(AtomicInteger resolutions, AtomicBoolean completed) {
            this.resolutions = resolutions;
            this.completed = completed;
        }

        @Override public CpfUnknownResultRecord register(CpfUnknownResultRecord value) { return value; }
        @Override public List<CpfUnknownResultRecord> find(String type, String status, int limit) { return List.of(); }
        @Override public void resolve(String unknownId, String status, String operatorId, String auditReason) {
            resolutions.incrementAndGet();
            completed.set(true);
        }
    }

    private static final class MutableClock extends Clock {
        private final AtomicReference<Instant> instant;
        private MutableClock(Instant instant) { this.instant = new AtomicReference<>(instant); }
        void advance(Duration duration) { instant.updateAndGet(value -> value.plus(duration)); }
        @Override public ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant.get(); }
    }
}
