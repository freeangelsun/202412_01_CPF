package com.cpf.core.common.reconciliation;

import com.cpf.core.api.resilience.CpfResilienceCallContext;
import com.cpf.core.api.resilience.CpfResilienceExecutor;
import com.cpf.core.api.resilience.CpfResilienceOutcome;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/** Verifies that UNKNOWN-result probes consume the shared resilience boundary without leaking identifiers. */
public final class CpfReconciliationResilienceConsumerHarness {
    private CpfReconciliationResilienceConsumerHarness() { }

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        successUsesReconcileAndSanitizedContext(clock);
        nonSuccessRemainsUnknown(clock, CpfResilienceOutcome.Status.UNKNOWN_RESULT, "provider secret=abc");
        nonSuccessRemainsUnknown(clock, CpfResilienceOutcome.Status.TIMEOUT, "READ_TIMEOUT");
        nonSuccessRemainsUnknown(clock, CpfResilienceOutcome.Status.FAILED, "REMOTE_FAILURE");
        nonSuccessRemainsUnknown(clock, CpfResilienceOutcome.Status.REJECTED, "BULKHEAD_FULL");
        compatibilityPathStillInvokesProbe(clock);
        System.out.println("CPF_RECONCILIATION_RESILIENCE_CONSUMER_HARNESS_PASS");
    }

    private static void successUsesReconcileAndSanitizedContext(Clock clock) {
        RecordingExecutor executor = new RecordingExecutor(clock, CpfResilienceOutcome.Status.SUCCESS, null);
        RecordingWork work = new RecordingWork(record("unknown-secret-001", "transaction-secret-001"));
        AtomicInteger resolved = new AtomicInteger();
        AtomicInteger probes = new AtomicInteger();
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                port(resolved), work, policy(), List.of(successProbe(probes)), "worker-a",
                clock, null, null, executor);
        worker.tick();
        require(executor.reconcileCalls.get() == 1 && executor.executeCalls.get() == 0,
                "reconciliation must use reconcile boundary");
        require(probes.get() == 1 && resolved.get() == 1, "success must resolve once");
        CpfResilienceCallContext context = executor.context;
        require(context != null && context.operationKind() == CpfResilienceCallContext.OperationKind.READ,
                "probe is a READ operation");
        require("CONSUMER".equals(context.attributes().get(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE)),
                "consumer span kind");
        require("RECONCILIATION_PROBE".equals(
                context.attributes().get(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE)),
                "trace segment");
        String visible = context.toString();
        require(!visible.contains("unknown-secret-001") && !visible.contains("transaction-secret-001"),
                "raw unknown and transaction identifiers must not enter resilience context");
        require(context.transactionId().startsWith("reconciliation-")
                        && context.idempotencyKey().startsWith("reconciliation-"),
                "hashed correlation identifiers");
    }

    private static void nonSuccessRemainsUnknown(
            Clock clock, CpfResilienceOutcome.Status status, String reasonCode) {
        RecordingExecutor executor = new RecordingExecutor(clock, status, reasonCode);
        RecordingWork work = new RecordingWork(record("unknown-" + status, "tx-" + status));
        AtomicInteger probes = new AtomicInteger();
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                port(new AtomicInteger()), work, policy(), List.of(successProbe(probes)), "worker-b",
                clock, null, null, executor);
        worker.tick();
        require(executor.reconcileCalls.get() == 1, status + " must use reconcile boundary");
        require(probes.get() == 0, status + " must not execute supplier in the fake executor");
        require(work.deferredReason != null && work.deferredReason.startsWith("PROBE_RESILIENCE_" + status),
                status + " must defer with typed reason");
        require(!work.deferredReason.toLowerCase().contains("secret"),
                status + " reason must not expose raw provider text");
        require(work.manualReason == null, status + " remains retryable before max attempts");
    }

    private static void compatibilityPathStillInvokesProbe(Clock clock) {
        RecordingWork work = new RecordingWork(record("unknown-direct", "tx-direct"));
        AtomicInteger probes = new AtomicInteger();
        AtomicInteger resolved = new AtomicInteger();
        new CpfReconciliationWorker(
                port(resolved), work, policy(), List.of(successProbe(probes)), "worker-direct",
                clock, null, null).tick();
        require(probes.get() == 1 && resolved.get() == 1,
                "compatibility constructor retains direct probe behavior");
    }

    private static CpfReconciliationRuntimePolicy policy() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(1L, true, 1_000L, 0, 1, 5, false, Set.of("PAYMENT"), 3, 2, 1_000L);
        return policy;
    }

    private static CpfUnknownResultRecord record(String id, String transactionId) {
        return new CpfUnknownResultRecord(id, "PAYMENT", "CHECK_PENDING", transactionId,
                null, "external", null, null, null,
                Instant.parse("2026-08-04T23:00:00Z"), null);
    }

    private static CpfReconciliationProbePort successProbe(AtomicInteger probes) {
        return new CpfReconciliationProbePort() {
            @Override public boolean supports(String unknownType) { return "PAYMENT".equals(unknownType); }
            @Override public ProbeResult probe(CpfUnknownResultRecord record) {
                probes.incrementAndGet();
                return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "confirmed");
            }
        };
    }

    private static CpfReconciliationPort port(AtomicInteger resolved) {
        return new CpfReconciliationPort() {
            @Override public CpfUnknownResultRecord register(CpfUnknownResultRecord record) { return record; }
            @Override public List<CpfUnknownResultRecord> find(String type, String status, int limit) { return List.of(); }
            @Override public void resolve(String id, String status, String operator, String reason) {
                resolved.incrementAndGet();
            }
        };
    }

    private static final class RecordingExecutor implements CpfResilienceExecutor {
        private final Clock clock;
        private final CpfResilienceOutcome.Status status;
        private final String reasonCode;
        private final AtomicInteger executeCalls = new AtomicInteger();
        private final AtomicInteger reconcileCalls = new AtomicInteger();
        private CpfResilienceCallContext context;

        private RecordingExecutor(Clock clock, CpfResilienceOutcome.Status status, String reasonCode) {
            this.clock = clock;
            this.status = status;
            this.reasonCode = reasonCode;
        }

        @Override
        public <T> CpfResilienceOutcome<T> execute(CpfResilienceCallContext context, Supplier<T> action) {
            executeCalls.incrementAndGet();
            throw new AssertionError("execute must not be used for reconciliation probes");
        }

        @Override
        public <T> CpfResilienceOutcome<T> reconcile(CpfResilienceCallContext context, Supplier<T> probe) {
            reconcileCalls.incrementAndGet();
            this.context = context;
            if (status == CpfResilienceOutcome.Status.SUCCESS) {
                return CpfResilienceOutcome.at(status, probe.get(), null, 1, 1L, clock);
            }
            return CpfResilienceOutcome.at(status, null, reasonCode, 1, 1L, clock);
        }
    }

    private static final class RecordingWork implements CpfReconciliationWorkPort {
        private final CpfUnknownResultRecord record;
        private boolean claimed;
        private String deferredReason;
        private String manualReason;

        private RecordingWork(CpfUnknownResultRecord record) { this.record = record; }

        @Override public List<WorkItem> claim(String type, int threshold, int limit, String worker, int lease) {
            if (claimed) return List.of();
            claimed = true;
            return List.of(new WorkItem(record, 0, 0L));
        }
        @Override public void defer(String id, String worker, Instant next, String action) {
            deferredReason = action;
        }
        @Override public void markManualReview(String id, String worker, String action) {
            manualReason = action;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
