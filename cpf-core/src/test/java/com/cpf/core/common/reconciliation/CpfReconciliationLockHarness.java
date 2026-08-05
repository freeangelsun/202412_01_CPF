package com.cpf.core.common.reconciliation;

import com.cpf.core.api.locking.CpfLockManager;
import com.cpf.core.internal.locking.DefaultCpfLockManager;
import com.cpf.core.internal.locking.InMemoryCpfLockStore;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** Verifies that two worker instances consume the common lease/fencing service. */
public final class CpfReconciliationLockHarness {
    private CpfReconciliationLockHarness() {}

    public static void main(String[] args) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        CpfLockManager manager = new DefaultCpfLockManager(new InMemoryCpfLockStore(), null, clock);
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(1, true, 1_000, 0, 1, 30, false, Set.of("PAYMENT"), 3, 2, 5_000);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger probes = new AtomicInteger();
        CpfUnknownResultRecord record = new CpfUnknownResultRecord(
                "u-1", "PAYMENT", "CHECK_PENDING", "tx-1", null, "external-1",
                null, null, null, clock.instant(), null);
        CpfReconciliationWorkPort work = new CpfReconciliationWorkPort() {
            @Override public List<WorkItem> claim(String type, int threshold, int limit, String worker, int lease) {
                return List.of(new WorkItem(record, 1, 1));
            }
            @Override public void defer(String unknownId, String workerId, Instant nextCheckAt, String nextAction) {}
            @Override public void markManualReview(String unknownId, String workerId, String nextAction) {}
        };
        CpfReconciliationProbePort probe = new CpfReconciliationProbePort() {
            @Override public boolean supports(String unknownType) { return "PAYMENT".equals(unknownType); }
            @Override public ProbeResult probe(CpfUnknownResultRecord ignored) {
                probes.incrementAndGet();
                entered.countDown();
                try { release.await(5, TimeUnit.SECONDS); }
                catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); }
                return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "token=secret");
            }
        };
        CpfReconciliationPort port = new CpfReconciliationPort() {
            @Override public CpfUnknownResultRecord register(CpfUnknownResultRecord value) { return value; }
            @Override public List<CpfUnknownResultRecord> find(String unknownType, String status, int limit) { return List.of(); }
            @Override public void resolve(String unknownId, String status, String operatorId, String auditReason) { }
        };
        CpfReconciliationWorker first = new CpfReconciliationWorker(
                port, work, policy, List.of(probe), "worker-a", clock, manager);
        CpfReconciliationWorker second = new CpfReconciliationWorker(
                port, work, policy, List.of(probe), "worker-b", clock, manager);
        ExecutorService callers = Executors.newFixedThreadPool(2);
        Future<?> firstRun = callers.submit(first::tick);
        if (!entered.await(5, TimeUnit.SECONDS)) throw new AssertionError("first worker did not enter probe");
        Future<?> secondRun = callers.submit(second::tick);
        secondRun.get(5, TimeUnit.SECONDS);
        if (probes.get() != 1) throw new AssertionError("lock did not prevent concurrent probe: " + probes.get());
        release.countDown();
        firstRun.get(5, TimeUnit.SECONDS);
        callers.shutdownNow();
        System.out.println("CPF_RECONCILIATION_LOCK_HARNESS_PASS");
    }
}
