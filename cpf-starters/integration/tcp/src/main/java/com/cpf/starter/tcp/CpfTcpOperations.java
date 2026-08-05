package com.cpf.starter.tcp;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** TCP correlation/UNKNOWN 운영 조회와 감사 가능한 reconcile facade입니다. */
public final class CpfTcpOperations {
    private final CpfTcpUnknownResultStore unknown;
    private final CpfTcpCorrelationRegistry correlations;

    public CpfTcpOperations(CpfTcpUnknownResultStore unknown, CpfTcpCorrelationRegistry correlations) {
        this.unknown = Objects.requireNonNull(unknown, "unknown");
        this.correlations = Objects.requireNonNull(correlations, "correlations");
    }

    public Snapshot snapshot() {
        return new Snapshot(
                correlations.pendingCount(),
                correlations.orphans().size(),
                unknown.snapshot().size(),
                unknown.durable());
    }

    /** 기존 호출 호환용이며 store의 현재 version을 읽어 동일한 CAS 경로를 사용합니다. */
    public boolean reconcile(String correlationId, String operator, String reason) {
        var current = unknown.findVersioned(correlationId);
        if (current.isEmpty()) {
            requireAudit(operator, reason);
            return unknown.reconcile(correlationId, 0L, operator, reason);
        }
        return reconcile(correlationId, current.get().version(), operator, reason);
    }

    public boolean reconcile(
            String correlationId,
            long expectedVersion,
            String operator,
            String reason) {
        requireAudit(operator, reason);
        return unknown.reconcile(correlationId, expectedVersion, operator, reason);
    }

    public List<Audit> audit() {
        return unknown.auditSnapshot().stream()
                .map(value -> new Audit(
                        value.correlationId(), value.operator(), value.reason(),
                        value.reconciled(), value.resultingVersion(), value.at()))
                .toList();
    }

    public void refresh() {
        unknown.refresh();
    }

    private static void requireAudit(String operator, String reason) {
        if (operator == null || operator.isBlank() || reason == null || reason.isBlank()) {
            throw new SecurityException("operator and reason required");
        }
    }

    public record Snapshot(int pending, int orphan, int unknownResult, boolean durableUnknownStore) {
    }

    public record Audit(
            String correlationId,
            String operator,
            String reason,
            boolean reconciled,
            long resultingVersion,
            Instant at) {
    }
}
