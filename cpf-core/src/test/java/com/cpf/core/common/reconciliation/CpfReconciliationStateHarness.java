package com.cpf.core.common.reconciliation;

import com.cpf.core.api.state.CpfOperationState;
import com.cpf.core.api.state.CpfStateOperations;
import com.cpf.core.api.state.CpfStateQueryResult;
import com.cpf.core.api.state.CpfStateSnapshot;
import com.cpf.core.internal.state.InMemoryCpfStateStore;
import com.cpf.core.service.state.DefaultCpfStateOperations;
import com.cpf.core.spi.state.CpfStateStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class CpfReconciliationStateHarness {
    private CpfReconciliationStateHarness() {}

    public static void main(String[] args) {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        successPath(clock);
        unavailableStoreBlocksProbe(clock);
        postResolutionStateFailureEscalates(clock);
        System.out.println("CPF_RECONCILIATION_STATE_HARNESS_PASS");
    }

    private static void successPath(Clock clock) {
        InMemoryCpfStateStore store = new InMemoryCpfStateStore();
        CpfStateOperations state = new DefaultCpfStateOperations(store, clock);
        RecordingWork work = new RecordingWork(record("unknown-success"));
        AtomicInteger resolved = new AtomicInteger();
        CpfReconciliationPort port = port(resolved);
        AtomicInteger probes = new AtomicInteger();
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                port,
                work,
                policy(),
                List.of(successProbe(probes)),
                "worker-a",
                clock,
                null,
                state);
        worker.tick();
        require(probes.get() == 1, "probe must run once");
        require(resolved.get() == 1, "record must resolve once");
        CpfStateQueryResult query = state.query("reconciliation:" + sha256("unknown-success"));
        require(query.status() == CpfStateQueryResult.Status.FOUND, "state must exist");
        require(query.snapshot().state() == CpfOperationState.SUCCEEDED, "state must be terminal success");
    }

    private static void unavailableStoreBlocksProbe(Clock clock) {
        CpfStateStore broken = new CpfStateStore() {
            @Override public Optional<CpfStateSnapshot> find(String stateKey) {
                throw new IllegalStateException("provider-secret");
            }
            @Override public WriteResult compareAndSet(
                    String stateKey, long expectedVersion, String operationId, String commandHash,
                    CpfStateSnapshot next) {
                throw new IllegalStateException("provider-secret");
            }
        };
        CpfStateOperations state = new DefaultCpfStateOperations(broken, clock);
        RecordingWork work = new RecordingWork(record("unknown-down"));
        AtomicInteger probes = new AtomicInteger();
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                port(new AtomicInteger()),
                work,
                policy(),
                List.of(successProbe(probes)),
                "worker-b",
                clock,
                null,
                state);
        worker.tick();
        require(probes.get() == 0, "probe must be blocked when state cannot be persisted");
        require("STATE_STORE_UNAVAILABLE".equals(work.deferredReason), "typed unavailable reason");
        require(!work.deferredReason.contains("provider-secret"), "provider error must not leak");
    }

    private static void postResolutionStateFailureEscalates(Clock clock) {
        InMemoryCpfStateStore delegate = new InMemoryCpfStateStore();
        AtomicInteger writes = new AtomicInteger();
        CpfStateStore failsAfterStart = new CpfStateStore() {
            @Override public Optional<CpfStateSnapshot> find(String stateKey) {
                return delegate.find(stateKey);
            }
            @Override public WriteResult compareAndSet(
                    String stateKey, long expectedVersion, String operationId, String commandHash,
                    CpfStateSnapshot next) {
                if (writes.incrementAndGet() > 1) {
                    return new WriteResult(Status.UNKNOWN, null);
                }
                return delegate.compareAndSet(stateKey, expectedVersion, operationId, commandHash, next);
            }
        };
        RecordingWork work = new RecordingWork(record("unknown-post-write"));
        AtomicInteger resolved = new AtomicInteger();
        CpfReconciliationWorker worker = new CpfReconciliationWorker(
                port(resolved),
                work,
                policy(),
                List.of(successProbe(new AtomicInteger())),
                "worker-c",
                clock,
                null,
                new DefaultCpfStateOperations(failsAfterStart, clock));
        worker.tick();
        require(resolved.get() == 1, "business resolution must still be visible");
        require(work.manualReason != null
                        && work.manualReason.startsWith("STATE_WRITE_AFTER_RESOLUTION_FAILED"),
                "post-resolution state loss must escalate to manual review");
    }

    private static CpfReconciliationRuntimePolicy policy() {
        CpfReconciliationRuntimePolicy policy = new CpfReconciliationRuntimePolicy();
        policy.replace(1L, true, 1_000L, 0, 1, 5, false, Set.of("PAYMENT"), 3, 2, 1_000L);
        return policy;
    }

    private static CpfReconciliationProbePort successProbe(AtomicInteger probes) {
        return new CpfReconciliationProbePort() {
            @Override public boolean supports(String unknownType) {
                return "PAYMENT".equals(unknownType);
            }
            @Override public ProbeResult probe(CpfUnknownResultRecord record) {
                probes.incrementAndGet();
                return new ProbeResult(Outcome.CONFIRMED_SUCCESS, "confirmed");
            }
        };
    }

    private static CpfReconciliationPort port(AtomicInteger resolved) {
        return new CpfReconciliationPort() {
            @Override public CpfUnknownResultRecord register(CpfUnknownResultRecord record) {
                return record;
            }
            @Override public List<CpfUnknownResultRecord> find(String type, String status, int limit) {
                return List.of();
            }
            @Override public void resolve(String unknownId, String status, String operatorId, String auditReason) {
                resolved.incrementAndGet();
            }
        };
    }

    private static CpfUnknownResultRecord record(String id) {
        return new CpfUnknownResultRecord(
                id, "PAYMENT", "CHECK_PENDING", "tx", "segment", "external", "", "", "",
                Instant.parse("2026-08-04T23:00:00Z"), null);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class RecordingWork implements CpfReconciliationWorkPort {
        private final CpfUnknownResultRecord record;
        private final AtomicBoolean claimed = new AtomicBoolean();
        private String deferredReason;
        private String manualReason;

        private RecordingWork(CpfUnknownResultRecord record) {
            this.record = record;
        }

        @Override
        public List<WorkItem> claim(
                String unknownType, int thresholdSeconds, int limit, String workerId, int leaseSeconds) {
            return claimed.compareAndSet(false, true) ? List.of(new WorkItem(record, 0, 0L)) : List.of();
        }

        @Override
        public void defer(String unknownId, String workerId, Instant nextCheckAt, String nextAction) {
            deferredReason = nextAction;
        }

        @Override
        public void markManualReview(String unknownId, String workerId, String nextAction) {
            manualReason = nextAction;
        }
    }
}
