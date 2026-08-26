package com.cpf.platform.operations.reconciliation;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.integration.resilience.api.CpfReconciliationRuntimeStatus;
import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateOperations;
import com.cpf.platform.operations.api.state.CpfStateTransitionRequest;
import com.cpf.platform.operations.api.state.CpfStateTransitionResult;
import com.cpf.security.api.CpfMaskingRuntime;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;

/** Distributed-claim worker for UNKNOWN results, optionally fenced by the common lock service. */
public final class CpfReconciliationWorker implements CpfReconciliationRuntimeStatus {
    private final CpfReconciliationPort port;
    private final CpfReconciliationWorkPort work;
    private final CpfReconciliationRuntimePolicy policy;
    private final List<CpfReconciliationProbePort> probes;
    private final String workerId;
    private final Clock clock;
    private final CpfLockManager lockManager;
    private final CpfStateOperations stateOperations;
    private final CpfResilienceExecutor resilienceExecutor;
    private final AtomicLong nextRun = new AtomicLong();
    private final Map<String, Circuit> circuits = new ConcurrentHashMap<>();
    private final Object circuitAllocationMonitor = new Object();
    private final AtomicLong circuitEvictionCount = new AtomicLong();
    private final AtomicLong circuitCapacityRejectionCount = new AtomicLong();
    private volatile Instant lastCircuitCapacityRejectionAt;

    /** Compatibility constructor. Product wiring should use the Clock-aware constructor. */
    public CpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            List<CpfReconciliationProbePort> probes,
            String workerId) {
        this(port, work, policy, probes, workerId, Clock.systemUTC(), null, null, null);
    }

    public CpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            List<CpfReconciliationProbePort> probes,
            String workerId,
            Clock clock,
            CpfLockManager lockManager) {
        this(port, work, policy, probes, workerId, clock, lockManager, null, null);
    }

    public CpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            List<CpfReconciliationProbePort> probes,
            String workerId,
            Clock clock,
            CpfLockManager lockManager,
            CpfStateOperations stateOperations) {
        this(port, work, policy, probes, workerId, clock, lockManager, stateOperations, null);
    }

    /**
     * Product constructor. Reconciliation probes consume the shared resilience boundary when one
     * is available; compatibility constructors intentionally retain the direct-probe path.
     */
    public CpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            List<CpfReconciliationProbePort> probes,
            String workerId,
            Clock clock,
            CpfLockManager lockManager,
            CpfStateOperations stateOperations,
            CpfResilienceExecutor resilienceExecutor) {
        this.port = Objects.requireNonNull(port, "port");
        this.work = Objects.requireNonNull(work, "work");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.workerId = normalizeWorkerId(workerId);
        this.probes = probes == null ? List.of() : List.copyOf(probes);
        this.clock = Objects.requireNonNull(clock, "clock");
        this.lockManager = lockManager;
        this.stateOperations = stateOperations;
        this.resilienceExecutor = resilienceExecutor;
    }

    @Scheduled(fixedDelayString = "${cpf.reconciliation.worker.tick-millis:1000}")
    public void tick() {
        CpfReconciliationRuntimePolicy.Snapshot snapshot = policy.current();
        long now = clock.millis();
        evictIdleCircuits(snapshot, now);
        if (!snapshot.enabled() || snapshot.unknownTypes().isEmpty()) return;
        long scheduled = nextRun.get();
        long next = saturatingAdd(now, snapshot.queryIntervalMillis());
        if (now < scheduled || !nextRun.compareAndSet(scheduled, next)) return;

        int remaining = snapshot.batchSize();
        if (lockManager != null) {
            CpfLockManager.RecoveryResult lockRecovery = lockManager.reconcileExpired(Math.min(remaining, 100));
            if (lockRecovery.status() == CpfLockManager.RecoveryStatus.UNKNOWN) {
                return;
            }
        }
        for (String configuredType : snapshot.unknownTypes()) {
            if (remaining <= 0) return;
            String unknownType = normalize(configuredType);
            if (unknownType.isEmpty()) continue;
            CpfLockManager.LockToken typeLock = acquireTypeLock(unknownType, snapshot, now);
            if (lockManager != null && typeLock == null) continue;
            try {
                TypeResult result = processType(unknownType, snapshot, remaining, typeLock);
                remaining = result.remaining();
                typeLock = result.lockToken();
            } finally {
                if (typeLock != null) {
                    lockManager.release(typeLock, "RECONCILIATION_TYPE_COMPLETED");
                }
            }
        }
    }

    private TypeResult processType(
            String unknownType,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            int remaining,
            CpfLockManager.LockToken typeLock) {
        CpfReconciliationProbePort probe = findProbe(unknownType);
        if (probe == null) {
            return moveMissingProbeToManualReview(unknownType, snapshot, remaining, typeLock);
        }

        Circuit circuit = circuitFor(unknownType, snapshot, clock.millis());
        if (circuit == null) return new TypeResult(remaining, typeLock);
        CpfLockManager.LockToken currentLock = typeLock;
        while (remaining > 0) {
            currentLock = renewIfNeeded(currentLock, snapshot);
            if (lockManager != null && currentLock == null) break;
            CircuitPermit permit = circuit.tryAcquire(clock.millis());
            if (permit == CircuitPermit.DENIED) break;
            List<CpfReconciliationWorkPort.WorkItem> claimed = work.claim(
                    unknownType, snapshot.thresholdSeconds(), 1, workerId, snapshot.leaseSeconds());
            if (claimed == null || claimed.isEmpty()) {
                circuit.releaseUnused(permit, clock.millis());
                break;
            }
            remaining--;
            ProcessResult result = process(claimed.getFirst(), probe, snapshot, circuit, permit, currentLock);
            if (result == ProcessResult.STALE_FENCE) break;
        }
        return new TypeResult(remaining, currentLock);
    }

    private CpfLockManager.LockToken acquireTypeLock(
            String unknownType, CpfReconciliationRuntimePolicy.Snapshot snapshot, long now) {
        if (lockManager == null) return null;
        long interval = Math.max(1L, snapshot.queryIntervalMillis());
        String requestId = workerId + ":" + unknownType + ":" + Math.floorDiv(now, interval);
        Duration lease = lockLease(snapshot);
        CpfLockManager.AcquireResult result = lockManager.acquire(
                lockKey(unknownType), workerId, requestId, lease);
        return result.status() == CpfLockManager.AcquireStatus.ACQUIRED
                        || result.status() == CpfLockManager.AcquireStatus.IDEMPOTENT_REPLAY
                ? result.token() : null;
    }

    private CpfLockManager.LockToken renewIfNeeded(
            CpfLockManager.LockToken token, CpfReconciliationRuntimePolicy.Snapshot snapshot) {
        if (lockManager == null || token == null) return token;
        Instant now = clock.instant();
        Duration lease = lockLease(snapshot);
        Instant renewAt = token.leaseUntil().minus(lease.dividedBy(2));
        if (now.isBefore(renewAt)) return token;
        CpfLockManager.RenewResult renewed = lockManager.renew(token, lease);
        return renewed.status() == CpfLockManager.RenewStatus.RENEWED ? renewed.token() : null;
    }

    private TypeResult moveMissingProbeToManualReview(
            String unknownType,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            int remaining,
            CpfLockManager.LockToken typeLock) {
        int moved = 0;
        CpfLockManager.LockToken currentLock = typeLock;
        while (moved < remaining) {
            currentLock = renewIfNeeded(currentLock, snapshot);
            if (!validFence(currentLock)) break;
            List<CpfReconciliationWorkPort.WorkItem> claimed = work.claim(
                    unknownType, snapshot.thresholdSeconds(), 1, workerId, snapshot.leaseSeconds());
            if (claimed == null || claimed.isEmpty()) break;
            if (!validFence(currentLock)) break;
            CpfReconciliationWorkPort.WorkItem item = claimed.getFirst();
            work.markManualReview(item.record().unknownId(), workerId,
                    "PROBE_NOT_FOUND:" + unknownType);
            moved++;
        }
        return new TypeResult(remaining - moved, currentLock);
    }

    private CpfReconciliationProbePort findProbe(String type) {
        for (CpfReconciliationProbePort probe : probes) {
            if (probe == null) continue;
            try {
                if (probe.supports(type)) return probe;
            } catch (RuntimeException ignored) {
                // A broken provider must not prevent another matching provider from being considered.
            }
        }
        return null;
    }

    private ProcessResult process(
            CpfReconciliationWorkPort.WorkItem item,
            CpfReconciliationProbePort probe,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            Circuit circuit,
            CircuitPermit permit,
            CpfLockManager.LockToken typeLock) {
        if (item == null || item.record() == null) {
            circuit.onFailure(clock.millis(), snapshot.circuitFailureThreshold(), snapshot.circuitOpenMillis());
            return ProcessResult.PROCESSED;
        }
        if (item.attemptCount() >= snapshot.maxAttempts()) {
            if (!validFence(typeLock)) return ProcessResult.STALE_FENCE;
            work.markManualReview(item.record().unknownId(), workerId,
                    "ATTEMPT_LIMIT_EXCEEDED:" + item.attemptCount());
            circuit.releaseUnused(permit, clock.millis());
            return ProcessResult.PROCESSED;
        }
        StateAttempt stateAttempt = beginState(item);
        if (stateAttempt.blocked()) {
            if (!validFence(typeLock)) return ProcessResult.STALE_FENCE;
            work.defer(item.record().unknownId(), workerId,
                    safePlus(clock.instant(), Duration.ofMillis(snapshot.queryIntervalMillis())),
                    stateAttempt.blockedReason());
            circuit.releaseUnused(permit, clock.millis());
            return ProcessResult.PROCESSED;
        }
        try {
            CpfReconciliationProbePort.ProbeResult result = executeProbe(item, probe);
            if (!validFence(typeLock)) {
                recordState(stateAttempt, CpfOperationState.UNKNOWN, "fence-lost", "FENCE_LOST_AFTER_PROBE");
                circuit.releaseUnused(permit, clock.millis());
                return ProcessResult.STALE_FENCE;
            }
            if (result == null || result.outcome() == CpfReconciliationProbePort.Outcome.PENDING) {
                if (!recordState(stateAttempt, CpfOperationState.UNKNOWN, "pending", "PROBE_PENDING")) {
                    work.defer(item.record().unknownId(), workerId,
                            safePlus(clock.instant(), Duration.ofMillis(snapshot.queryIntervalMillis())),
                            "STATE_WRITE_FAILED:PROBE_PENDING");
                    circuit.onFailure(clock.millis(), snapshot.circuitFailureThreshold(), snapshot.circuitOpenMillis());
                    return ProcessResult.PROCESSED;
                }
                work.defer(item.record().unknownId(), workerId,
                        safePlus(clock.instant(), Duration.ofMillis(snapshot.queryIntervalMillis())), "PROBE_PENDING");
                circuit.onSuccess(clock.millis());
                return ProcessResult.PROCESSED;
            }
            boolean success = result.outcome() == CpfReconciliationProbePort.Outcome.CONFIRMED_SUCCESS;
            String status = success ? "RESOLVED_SUCCESS" : "RESOLVED_FAILED";
            if (snapshot.manualResolutionRequired()) {
                recordState(stateAttempt, CpfOperationState.UNKNOWN, "manual-review", status);
                work.markManualReview(item.record().unknownId(), workerId,
                        status + ":" + safe(result.reason()));
            } else {
                port.resolve(item.record().unknownId(), status, "CPF_RECONCILIATION",
                        "automatic result confirmation: " + safe(result.reason()));
                CpfOperationState terminal = success ? CpfOperationState.SUCCEEDED : CpfOperationState.FAILED;
                if (!recordState(stateAttempt, terminal, success ? "success" : "failure", status)) {
                    work.markManualReview(item.record().unknownId(), workerId,
                            "STATE_WRITE_AFTER_RESOLUTION_FAILED:" + status);
                }
            }
            circuit.onSuccess(clock.millis());
            return ProcessResult.PROCESSED;
        } catch (RuntimeException failure) {
            circuit.onFailure(clock.millis(), snapshot.circuitFailureThreshold(), snapshot.circuitOpenMillis());
            if (!validFence(typeLock)) {
                recordState(stateAttempt, CpfOperationState.UNKNOWN, "failure-fence-lost", "FENCE_LOST_AFTER_FAILURE");
                return ProcessResult.STALE_FENCE;
            }
            String reason = probeFailureReason(failure);
            recordState(stateAttempt, CpfOperationState.UNKNOWN, "probe-error", reason);
            if (item.attemptCount() + 1 >= snapshot.maxAttempts()) {
                work.markManualReview(item.record().unknownId(), workerId,
                        "ATTEMPT_LIMIT_EXCEEDED:" + reason);
            } else {
                work.defer(item.record().unknownId(), workerId,
                        safePlus(clock.instant(), Duration.ofMillis(snapshot.queryIntervalMillis())), reason);
            }
            return ProcessResult.PROCESSED;
        }
    }

    private CpfReconciliationProbePort.ProbeResult executeProbe(
            CpfReconciliationWorkPort.WorkItem item,
            CpfReconciliationProbePort probe) {
        if (resilienceExecutor == null) return probe.probe(item.record());
        CpfUnknownResultRecord record = item.record();
        String type = normalize(record.unknownType()).toLowerCase(Locale.ROOT);
        // UNKNOWN 대사는 원 거래의 transactionId를 그대로 유지해야 한다. 누락/Legacy ID를
        // 새 ID로 바꾸면 원 거래와 Reconcile의 lineage가 끊기므로 fail-closed한다.
        String transactionId = CpfTransactionIds.requireCanonical(record.transactionId());
        String idempotencyKey = "reconciliation-"
                + sha256(record.unknownId() + ":" + item.attemptCount());
        CpfResilienceCallContext context = CpfResilienceCallContext.recoveredLineage(
                "cpf.reconciliation.probe." + type,
                transactionId,
                idempotencyKey,
                Map.of(
                        CpfResilienceCallContext.OPERATION_KIND_ATTRIBUTE, "READ",
                        CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE, "CONSUMER",
                        CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE, "RECONCILIATION_PROBE"),
                clock);
        CpfResilienceOutcome<CpfReconciliationProbePort.ProbeResult> outcome =
                resilienceExecutor.reconcile(context, () -> probe.probe(record));
        if (outcome == null) throw new ProbeExecutionException("RESILIENCE_NULL_OUTCOME");
        if (outcome.status() == CpfResilienceOutcome.Status.SUCCESS) {
            if (outcome.value() == null) throw new ProbeExecutionException("RESILIENCE_NULL_RESULT");
            return outcome.value();
        }
        String reasonCode = canonicalReasonCode(outcome.reasonCode());
        String code = "RESILIENCE_" + outcome.status().name();
        if (!reasonCode.isEmpty()) code += ":" + reasonCode;
        throw new ProbeExecutionException(code);
    }

    private static String probeFailureReason(RuntimeException failure) {
        if (failure instanceof ProbeExecutionException probeFailure) {
            return "PROBE_" + probeFailure.reasonCode();
        }
        return "PROBE_ERROR:" + failure.getClass().getSimpleName();
    }

    private static String canonicalReasonCode(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 64 || !normalized.matches("[A-Z0-9][A-Z0-9._-]*")) {
            return "UNSPECIFIED";
        }
        return normalized;
    }


    @Override
    public RuntimeSnapshot reconciliationRuntimeSnapshot() {
        CpfReconciliationRuntimePolicy.Snapshot snapshot = policy.current();
        int size = circuits.size();
        long rejected = circuitCapacityRejectionCount.get();
        Health health = size >= snapshot.maximumCircuitEntries() ? Health.CAPACITY_EXHAUSTED
                : rejected > 0L ? Health.DEGRADED : Health.UP;
        return new RuntimeSnapshot(
                health,
                size,
                snapshot.maximumCircuitEntries(),
                Duration.ofMillis(snapshot.circuitIdleTtlMillis()),
                circuitEvictionCount.get(),
                rejected,
                lastCircuitCapacityRejectionAt,
                clock.instant());
    }

    private Circuit circuitFor(
            String unknownType,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            long nowMillis) {
        Circuit existing = circuits.get(unknownType);
        if (existing != null) {
            existing.touch(nowMillis);
            return existing;
        }
        synchronized (circuitAllocationMonitor) {
            existing = circuits.get(unknownType);
            if (existing != null) {
                existing.touch(nowMillis);
                return existing;
            }
            evictIdleCircuitsLocked(snapshot, nowMillis);
            if (circuits.size() >= snapshot.maximumCircuitEntries()) {
                circuitCapacityRejectionCount.incrementAndGet();
                lastCircuitCapacityRejectionAt = clock.instant();
                return null;
            }
            Circuit created = new Circuit(nowMillis);
            circuits.put(unknownType, created);
            return created;
        }
    }

    private void evictIdleCircuits(
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            long nowMillis) {
        synchronized (circuitAllocationMonitor) {
            evictIdleCircuitsLocked(snapshot, nowMillis);
        }
    }

    private void evictIdleCircuitsLocked(
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            long nowMillis) {
        circuits.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    Circuit circuit = entry.getValue();
                    if (circuit.idleAndEvictable(nowMillis, snapshot.circuitIdleTtlMillis())
                            && circuits.remove(entry.getKey(), circuit)) {
                        circuitEvictionCount.incrementAndGet();
                    }
                });
    }


    private StateAttempt beginState(CpfReconciliationWorkPort.WorkItem item) {
        if (stateOperations == null) return StateAttempt.disabled();
        String unknownId = item.record().unknownId();
        String seed = workerId + ":" + unknownId + ":" + item.attemptCount();
        String stateKey = "reconciliation:" + sha256(unknownId);
        String operationSeed = "reconciliation:" + sha256(seed);
        CpfStateTransitionResult started = stateOperations.start(
                stateKey, operationSeed + ":start", workerId, "RECONCILIATION_PROBE_START");
        if (started.applied() && started.snapshot() != null) {
            return new StateAttempt(stateKey, started.snapshot().version(), operationSeed, "");
        }
        String blocked = switch (started.status()) {
            case STORE_UNAVAILABLE -> "STATE_STORE_UNAVAILABLE";
            case AUDIT_UNAVAILABLE -> "STATE_AUDIT_UNAVAILABLE";
            case UNKNOWN_RESULT -> "STATE_RESULT_UNKNOWN";
            case VERSION_CONFLICT -> "STATE_VERSION_CONFLICT";
            case OPERATION_CONFLICT -> "STATE_OPERATION_CONFLICT";
            case RESOURCE_EXHAUSTED -> "STATE_RESOURCE_EXHAUSTED";
            case INVALID_TRANSITION -> "STATE_TERMINAL_OR_INVALID";
            case NOT_FOUND -> "STATE_NOT_FOUND";
            case APPLIED, IDEMPOTENT_REPLAY -> "STATE_SNAPSHOT_MISSING";
        };
        return new StateAttempt(stateKey, -1L, operationSeed, blocked);
    }

    private boolean recordState(
            StateAttempt attempt,
            CpfOperationState target,
            String operationSuffix,
            String reason) {
        if (!attempt.enabled()) return true;
        CpfStateTransitionResult transition = stateOperations.transition(new CpfStateTransitionRequest(
                attempt.stateKey(),
                attempt.version(),
                target,
                attempt.operationSeed() + ":" + operationSuffix,
                workerId,
                safe(reason)));
        return transition.applied();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    Objects.toString(value, "UNKNOWN").getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private boolean validFence(CpfLockManager.LockToken token) {
        return lockManager == null
                || (token != null && lockManager.validateFence(token.key(), token.fencingToken()));
    }

    private static Instant safePlus(Instant instant, Duration duration) {
        try {
            return instant.plus(duration);
        } catch (RuntimeException overflow) {
            return Instant.MAX;
        }
    }

    private static Duration lockLease(CpfReconciliationRuntimePolicy.Snapshot snapshot) {
        return Duration.ofSeconds(Math.max(1L, snapshot.leaseSeconds()));
    }

    private static String lockKey(String unknownType) {
        return "cpf:reconciliation:" + unknownType;
    }

    private static String normalize(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeWorkerId(String value) {
        String normalized = value == null || value.isBlank() ? "CPF-RECONCILIATION" : value.trim();
        if (normalized.length() > 128) throw new IllegalArgumentException("workerId exceeds 128 characters");
        return normalized;
    }

    private static String safe(String value) {
        return CpfMaskingRuntime.mask(value == null ? "" : value, 300);
    }

    private static long saturatingAdd(long left, long right) {
        try {
            return Math.addExact(left, Math.max(0L, right));
        } catch (ArithmeticException overflow) {
            return Long.MAX_VALUE;
        }
    }

    private static final class ProbeExecutionException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String reasonCode;

        private ProbeExecutionException(String reasonCode) {
            super(null, null, false, false);
            this.reasonCode = Objects.requireNonNull(reasonCode, "reasonCode");
        }

        private String reasonCode() {
            return reasonCode;
        }
    }

    private record TypeResult(int remaining, CpfLockManager.LockToken lockToken) {}
    private record StateAttempt(String stateKey, long version, String operationSeed, String blockedReason) {
        static StateAttempt disabled() {
            return new StateAttempt("", -1L, "", "");
        }
        boolean enabled() {
            return version >= 0L;
        }
        boolean blocked() {
            return !blockedReason.isEmpty();
        }
    }
    private enum ProcessResult { PROCESSED, STALE_FENCE }
    private enum CircuitPermit { CLOSED, HALF_OPEN, DENIED }

    private static final class Circuit {
        private int failures;
        private long openUntil;
        private boolean halfOpenInFlight;
        private long lastAccessMillis;

        private Circuit(long createdAtMillis) {
            this.lastAccessMillis = createdAtMillis;
        }

        synchronized void touch(long now) {
            lastAccessMillis = Math.max(lastAccessMillis, now);
        }

        synchronized CircuitPermit tryAcquire(long now) {
            touch(now);
            if (openUntil > now) return CircuitPermit.DENIED;
            if (openUntil > 0L) {
                if (halfOpenInFlight) return CircuitPermit.DENIED;
                halfOpenInFlight = true;
                return CircuitPermit.HALF_OPEN;
            }
            return CircuitPermit.CLOSED;
        }

        synchronized void onSuccess(long now) {
            touch(now);
            failures = 0;
            openUntil = 0L;
            halfOpenInFlight = false;
        }

        synchronized void onFailure(long now, int threshold, long openMillis) {
            touch(now);
            failures++;
            if (halfOpenInFlight || failures >= threshold) {
                openUntil = saturatingAdd(now, openMillis);
                failures = 0;
            }
            halfOpenInFlight = false;
        }

        synchronized void releaseUnused(CircuitPermit permit, long now) {
            touch(now);
            if (permit == CircuitPermit.HALF_OPEN) halfOpenInFlight = false;
        }

        synchronized boolean idleAndEvictable(long now, long idleTtlMillis) {
            if (halfOpenInFlight) return false;
            long elapsed = now >= lastAccessMillis ? now - lastAccessMillis : 0L;
            return elapsed >= idleTtlMillis;
        }
    }
}
