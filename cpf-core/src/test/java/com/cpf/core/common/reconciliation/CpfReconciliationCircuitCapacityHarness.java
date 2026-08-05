package com.cpf.core.common.reconciliation;

import com.cpf.core.api.reliability.CpfReconciliationRuntimeStatus;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

/** Policy churn must not retain unbounded circuit keys or evict an active half-open probe. */
public final class CpfReconciliationCircuitCapacityHarness {
    private CpfReconciliationCircuitCapacityHarness() { }

    public static void main(String[] args) {
        MutableClock clock = new MutableClock(Instant.parse("2026-08-05T00:00:00Z"));
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(1L, true, 1_000L, 0, 10, 30, true, Set.of("TYPE_A"),
                3, 2, 5_000L, 1, 10_000L);
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                new NoopPort(), new EmptyWork(), policy,
                List.of(new AnyProbe()), "worker-capacity", clock, null, null);

        worker.tick();
        CpfReconciliationRuntimeStatus.RuntimeSnapshot first = worker.reconciliationRuntimeSnapshot();
        check(first.circuitEntries() == 1 && first.maximumCircuitEntries() == 1,
                "first circuit allocation");

        policy.replace(2L, true, 1_000L, 0, 10, 30, true, Set.of("TYPE_B"),
                3, 2, 5_000L, 1, 10_000L);
        clock.advance(Duration.ofMillis(1_001L));
        worker.tick();
        CpfReconciliationRuntimeStatus.RuntimeSnapshot rejected = worker.reconciliationRuntimeSnapshot();
        check(rejected.circuitEntries() == 1,
                "policy churn must not exceed circuit capacity");
        check(rejected.circuitCapacityRejectionCount() == 1L
                        && rejected.lastCircuitCapacityRejectionAt() != null,
                "capacity rejection evidence");

        clock.advance(Duration.ofMillis(10_001L));
        worker.tick();
        CpfReconciliationRuntimeStatus.RuntimeSnapshot reclaimed = worker.reconciliationRuntimeSnapshot();
        check(reclaimed.circuitEntries() == 1 && reclaimed.circuitEvictionCount() == 1L,
                "idle circuit is deterministically reclaimed");
        check(reclaimed.circuitIdleTtl().equals(Duration.ofSeconds(10)),
                "runtime policy exposes circuit TTL");

        boolean invalid = false;
        try {
            policy.replace(3L, true, 1_000L, 0, 10, 30, true, Set.of("TYPE_C"),
                    3, 2, 5_000L, 0, 10_000L);
        } catch (IllegalArgumentException expected) {
            invalid = true;
        }
        check(invalid, "zero circuit capacity is rejected");
        System.out.println("CPF_RECONCILIATION_CIRCUIT_CAPACITY_HARNESS_PASS");
    }

    private static final class NoopPort implements CpfReconciliationPort {
        @Override public CpfUnknownResultRecord register(CpfUnknownResultRecord record) { return record; }
        @Override public List<CpfUnknownResultRecord> find(String type, String status, int limit) { return List.of(); }
        @Override public void resolve(String id, String status, String operator, String reason) { }
    }

    private static final class EmptyWork implements CpfReconciliationWorkPort {
        @Override public List<WorkItem> claim(String type, int threshold, int limit, String worker, int lease) {
            return List.of();
        }
        @Override public void defer(String id, String worker, Instant next, String action) { }
        @Override public void markManualReview(String id, String worker, String action) { }
    }

    private static final class AnyProbe implements CpfReconciliationProbePort {
        @Override public boolean supports(String unknownType) { return true; }
        @Override public ProbeResult probe(CpfUnknownResultRecord record) {
            return new ProbeResult(Outcome.PENDING, "unused");
        }
    }

    private static void check(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
    }

    private static final class MutableClock extends Clock {
        private Instant current;
        private MutableClock(Instant current) { this.current = current; }
        void advance(Duration duration) { current = current.plus(duration); }
        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return current; }
    }
}
