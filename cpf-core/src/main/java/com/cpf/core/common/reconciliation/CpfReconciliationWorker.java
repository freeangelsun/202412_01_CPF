package com.cpf.core.common.reconciliation;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/** 분산 claim 후 외부 Probe를 실행하는 결과불명 자동확인 Worker입니다. */
public final class CpfReconciliationWorker {
    private final CpfReconciliationPort port;
    private final CpfReconciliationWorkPort work;
    private final CpfReconciliationRuntimePolicy policy;
    private final List<CpfReconciliationProbePort> probes;
    private final String workerId;
    private final AtomicLong nextRun = new AtomicLong();
    private final Map<String, Circuit> circuits = new ConcurrentHashMap<>();

    public CpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            List<CpfReconciliationProbePort> probes,
            String workerId) {
        this.port = port;
        this.work = work;
        this.policy = policy;
        this.workerId =
                workerId == null || workerId.isBlank()
                        ? "CPF-RECONCILIATION"
                        : workerId;
        this.probes = probes == null ? List.of() : List.copyOf(probes);
    }

    @Scheduled(fixedDelayString = "${cpf.reconciliation.worker.tick-millis:1000}")
    public void tick() {
        CpfReconciliationRuntimePolicy.Snapshot snapshot = policy.current();
        long now = System.currentTimeMillis();
        if (!snapshot.enabled() || snapshot.unknownTypes().isEmpty()) {
            return;
        }
        long scheduled = nextRun.get();
        if (now < scheduled
                || !nextRun.compareAndSet(scheduled, now + snapshot.queryIntervalMillis())) {
            return;
        }

        int remaining = snapshot.batchSize();
        for (String configuredType : snapshot.unknownTypes()) {
            if (remaining <= 0) {
                return;
            }
            String unknownType = normalize(configuredType);
            CpfReconciliationProbePort probe = findProbe(unknownType);
            if (probe == null) {
                remaining -= moveMissingProbeToManualReview(unknownType, snapshot, remaining);
                continue;
            }

            Circuit circuit = circuits.computeIfAbsent(unknownType, ignored -> new Circuit());
            while (remaining > 0) {
                CircuitPermit permit =
                        circuit.tryAcquire(System.currentTimeMillis(), snapshot.circuitOpenMillis());
                if (permit == CircuitPermit.DENIED) {
                    break;
                }
                List<CpfReconciliationWorkPort.WorkItem> claimed =
                        work.claim(
                                unknownType,
                                snapshot.thresholdSeconds(),
                                1,
                                workerId,
                                snapshot.leaseSeconds());
                if (claimed.isEmpty()) {
                    circuit.releaseUnused(permit);
                    break;
                }
                remaining--;
                process(claimed.getFirst(), probe, snapshot, circuit, permit);
            }
        }
    }

    private int moveMissingProbeToManualReview(
            String unknownType,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            int remaining) {
        int moved = 0;
        while (moved < remaining) {
            List<CpfReconciliationWorkPort.WorkItem> claimed =
                    work.claim(
                            unknownType,
                            snapshot.thresholdSeconds(),
                            1,
                            workerId,
                            snapshot.leaseSeconds());
            if (claimed.isEmpty()) {
                break;
            }
            CpfReconciliationWorkPort.WorkItem item = claimed.getFirst();
            work.markManualReview(
                    item.record().unknownId(),
                    workerId,
                    "PROBE_NOT_FOUND:" + unknownType);
            moved++;
        }
        return moved;
    }

    private CpfReconciliationProbePort findProbe(String type) {
        for (CpfReconciliationProbePort probe : probes) {
            if (probe.supports(type)) {
                return probe;
            }
        }
        return null;
    }

    private void process(
            CpfReconciliationWorkPort.WorkItem item,
            CpfReconciliationProbePort probe,
            CpfReconciliationRuntimePolicy.Snapshot snapshot,
            Circuit circuit,
            CircuitPermit permit) {
        if (item.attemptCount() > snapshot.maxAttempts()) {
            work.markManualReview(
                    item.record().unknownId(),
                    workerId,
                    "ATTEMPT_LIMIT_EXCEEDED:" + item.attemptCount());
            circuit.releaseUnused(permit);
            return;
        }
        try {
            CpfReconciliationProbePort.ProbeResult result = probe.probe(item.record());
            if (result == null
                    || result.outcome() == CpfReconciliationProbePort.Outcome.PENDING) {
                work.defer(
                        item.record().unknownId(),
                        workerId,
                        Instant.now().plusMillis(snapshot.queryIntervalMillis()),
                        "PROBE_PENDING");
                circuit.onSuccess();
                return;
            }
            String status =
                    result.outcome()
                                    == CpfReconciliationProbePort.Outcome.CONFIRMED_SUCCESS
                            ? "RESOLVED_SUCCESS"
                            : "RESOLVED_FAILED";
            if (snapshot.manualResolutionRequired()) {
                work.markManualReview(
                        item.record().unknownId(),
                        workerId,
                        status + ":" + safe(result.reason()));
            } else {
                port.resolve(
                        item.record().unknownId(),
                        status,
                        "CPF_RECONCILIATION",
                        "자동 결과 확인: " + safe(result.reason()));
            }
            circuit.onSuccess();
        } catch (RuntimeException failure) {
            circuit.onFailure(
                    System.currentTimeMillis(),
                    snapshot.circuitFailureThreshold(),
                    snapshot.circuitOpenMillis());
            String reason = "PROBE_ERROR:" + failure.getClass().getSimpleName();
            if (item.attemptCount() >= snapshot.maxAttempts()) {
                work.markManualReview(
                        item.record().unknownId(),
                        workerId,
                        "ATTEMPT_LIMIT_EXCEEDED:" + reason);
            } else {
                work.defer(
                        item.record().unknownId(),
                        workerId,
                        Instant.now().plusMillis(snapshot.queryIntervalMillis()),
                        reason);
            }
        }
    }

    private String normalize(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        String sanitized =
                value.replaceAll(
                        "(?i)(password|token|secret|authorization|api[-_]?key)\\s*[:=]\\s*[^,;\\s]+",
                        "$1=***");
        return sanitized.length() > 300 ? sanitized.substring(0, 300) : sanitized;
    }

    private enum CircuitPermit {
        CLOSED,
        HALF_OPEN,
        DENIED
    }

    private static final class Circuit {
        private int failures;
        private long openUntil;
        private boolean halfOpenInFlight;

        synchronized CircuitPermit tryAcquire(long now, long openMillis) {
            if (openUntil > now) {
                return CircuitPermit.DENIED;
            }
            if (openUntil > 0L) {
                if (halfOpenInFlight) {
                    return CircuitPermit.DENIED;
                }
                halfOpenInFlight = true;
                return CircuitPermit.HALF_OPEN;
            }
            return CircuitPermit.CLOSED;
        }

        synchronized void onSuccess() {
            failures = 0;
            openUntil = 0L;
            halfOpenInFlight = false;
        }

        synchronized void onFailure(long now, int threshold, long openMillis) {
            failures++;
            if (halfOpenInFlight || failures >= threshold) {
                openUntil = now + openMillis;
                failures = 0;
            }
            halfOpenInFlight = false;
        }

        synchronized void releaseUnused(CircuitPermit permit) {
            if (permit == CircuitPermit.HALF_OPEN) {
                halfOpenInFlight = false;
            }
        }
    }
}
