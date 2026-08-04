package com.cpf.core.common.reconciliation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class CpfReconciliationWorkerSafetyTest {
    @Test
    void enabledWorkerRequiresExplicitAllowlist() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        policy.replace(
                                1L,
                                true,
                                1_000L,
                                0,
                                10,
                                10,
                                true,
                                Set.of(),
                                3,
                                2,
                                1_000L));
    }

    @Test
    void batchSizeIsGlobalRateLimitAcrossAllowlistedTypes() {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        repository.add("A", item("a2", "A", 1));
        repository.add("B", item("b1", "B", 1));
        repository.add("B", item("b2", "B", 1));
        CpfReconciliationRuntimePolicy policy = policy(Set.of("A", "B"), 2, 4, 2, true);
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy,
                        List.of(pendingProbe("A"), pendingProbe("B")),
                        "worker-1");

        worker.tick();

        assertEquals(2, repository.claimed);
        assertEquals(2, repository.deferred.size());
    }

    @Test
    void attemptLimitMovesItemToManualReviewWithoutProbe() {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 3));
        AtomicInteger probes = new AtomicInteger();
        CpfReconciliationProbePort probe =
                new CpfReconciliationProbePort() {
                    @Override
                    public boolean supports(String unknownType) {
                        return "A".equals(unknownType);
                    }

                    @Override
                    public ProbeResult probe(CpfUnknownResultRecord record) {
                        probes.incrementAndGet();
                        return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "ok");
                    }
                };
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 2, 2, false),
                        List.of(probe),
                        "worker-1");

        worker.tick();

        assertEquals(0, probes.get());
        assertEquals(List.of("a1:ATTEMPT_LIMIT_EXCEEDED:3"), repository.manual);
    }

    @Test
    void missingProbeIsFailClosedToManualReview() {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 3, 2, false),
                        List.of(),
                        "worker-1");

        worker.tick();

        assertEquals(List.of("a1:PROBE_NOT_FOUND:A"), repository.manual);
        assertTrue(repository.resolved.isEmpty());
    }

    @Test
    void manualResolutionBoundaryPreventsAutomaticFinalization() {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 3, 2, true),
                        List.of(successProbe("A")),
                        "worker-1");

        worker.tick();

        assertTrue(repository.resolved.isEmpty());
        assertEquals(List.of("a1:RESOLVED_SUCCESS:confirmed"), repository.manual);
    }

    @Test
    void automaticResolutionRequiresExplicitPolicyAndRecordsAuditReason() {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 3, 2, false),
                        List.of(successProbe("A")),
                        "worker-1");

        worker.tick();

        assertEquals(1, repository.resolved.size());
        assertTrue(repository.resolved.getFirst().contains("CPF_RECONCILIATION"));
        assertTrue(repository.resolved.getFirst().contains("자동 결과 확인"));
    }

    @Test
    void circuitStopsProbeStormAndHalfOpenSuccessClosesIt() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        repository.add("A", item("a2", "A", 1));
        repository.add("A", item("a3", "A", 1));
        AtomicInteger calls = new AtomicInteger();
        CpfReconciliationProbePort probe =
                new CpfReconciliationProbePort() {
                    @Override
                    public boolean supports(String unknownType) {
                        return "A".equals(unknownType);
                    }

                    @Override
                    public ProbeResult probe(CpfUnknownResultRecord record) {
                        if (calls.incrementAndGet() <= 2) {
                            throw new IllegalStateException("dependency down");
                        }
                        return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "recovered");
                    }
                };
        CpfReconciliationWorker worker =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 3, 5, 2, false),
                        List.of(probe),
                        "worker-1");

        worker.tick();
        assertEquals(2, calls.get());
        assertEquals(1, repository.remaining("A"));

        Thread.sleep(1_100L);
        worker.tick();
        assertEquals(3, calls.get());
        assertEquals(1, repository.resolved.size());
    }


    @Test
    void twoWorkerInstancesClaimOneItemOnlyOnce() throws Exception {
        FakeRepository repository = new FakeRepository();
        repository.add("A", item("a1", "A", 1));
        CpfReconciliationWorker first =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 3, 2, true),
                        List.of(pendingProbe("A")),
                        "worker-1");
        CpfReconciliationWorker second =
                new CpfReconciliationWorker(
                        repository,
                        repository,
                        policy(Set.of("A"), 1, 3, 2, true),
                        List.of(pendingProbe("A")),
                        "worker-2");

        Thread a = new Thread(first::tick);
        Thread b = new Thread(second::tick);
        a.start();
        b.start();
        a.join();
        b.join();

        assertEquals(1, repository.claimed);
        assertEquals(1, repository.deferred.size());
    }

    private CpfReconciliationRuntimePolicy policy(
            Set<String> types,
            int batchSize,
            int maxAttempts,
            int circuitThreshold,
            boolean manual) {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(
                1L,
                true,
                1_000L,
                0,
                batchSize,
                10,
                manual,
                types,
                maxAttempts,
                circuitThreshold,
                1_000L);
        return policy;
    }

    private CpfReconciliationProbePort pendingProbe(String type) {
        return probe(type, CpfReconciliationProbePort.Outcome.PENDING, "pending");
    }

    private CpfReconciliationProbePort successProbe(String type) {
        return probe(type, CpfReconciliationProbePort.Outcome.CONFIRMED_SUCCESS, "confirmed");
    }

    private CpfReconciliationProbePort probe(
            String type,
            CpfReconciliationProbePort.Outcome outcome,
            String reason) {
        return new CpfReconciliationProbePort() {
            @Override
            public boolean supports(String unknownType) {
                return type.equals(unknownType);
            }

            @Override
            public ProbeResult probe(CpfUnknownResultRecord record) {
                return new ProbeResult(outcome, reason);
            }
        };
    }

    private CpfReconciliationWorkPort.WorkItem item(String id, String type, int attempt) {
        CpfUnknownResultRecord record =
                new CpfUnknownResultRecord(
                        id,
                        type,
                        "CHECK_PENDING",
                        "tx",
                        "segment",
                        "external",
                        "UNKNOWN",
                        "result unknown",
                        "RECONCILE",
                        Instant.now().minusSeconds(60),
                        null);
        return new CpfReconciliationWorkPort.WorkItem(record, attempt, attempt);
    }

    private static final class FakeRepository
            implements CpfReconciliationPort, CpfReconciliationWorkPort {
        private final Map<String, Deque<WorkItem>> items = new LinkedHashMap<>();
        private final List<String> deferred = new ArrayList<>();
        private final List<String> manual = new ArrayList<>();
        private final List<String> resolved = new ArrayList<>();
        private int claimed;

        void add(String type, WorkItem item) {
            items.computeIfAbsent(type, ignored -> new ArrayDeque<>()).add(item);
        }

        int remaining(String type) {
            return items.getOrDefault(type, new ArrayDeque<>()).size();
        }

        @Override
        public CpfUnknownResultRecord register(CpfUnknownResultRecord record) {
            return record;
        }

        @Override
        public List<CpfUnknownResultRecord> find(String unknownType, String status, int limit) {
            return List.of();
        }

        @Override
        public void resolve(String unknownId, String status, String operatorId, String auditReason) {
            resolved.add(unknownId + ":" + status + ":" + operatorId + ":" + auditReason);
        }

        @Override
        public List<WorkItem> claim(
                String unknownType,
                int thresholdSeconds,
                int limit,
                String workerId,
                int leaseSeconds) {
            Deque<WorkItem> queue = items.get(unknownType);
            if (queue == null || queue.isEmpty()) {
                return List.of();
            }
            claimed++;
            return List.of(queue.removeFirst());
        }

        @Override
        public void defer(
                String unknownId,
                String workerId,
                Instant nextCheckAt,
                String nextAction) {
            deferred.add(unknownId + ":" + nextAction);
        }

        @Override
        public void markManualReview(String unknownId, String workerId, String nextAction) {
            manual.add(unknownId + ":" + nextAction);
        }
    }
}
