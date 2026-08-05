package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeControlPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/** Controller leader lease를 소유한 한 인스턴스만 전역 Runtime 상태 전이를 수행합니다. */
public class CpfRuntimeControlReconciler {
    private static final Logger log = LoggerFactory.getLogger(CpfRuntimeControlReconciler.class);
    private final CpfRuntimeControlPlaneRepository repository;
    private final CpfRuntimeControlPlane controlPlane;
    private final String controllerId;
    private final int leaseSeconds;
    private final int ackTimeoutSeconds;
    private final CpfRuntimeAutoRollbackPolicy autoRollbackPolicy;

    public CpfRuntimeControlReconciler(
            CpfRuntimeControlPlaneRepository repository,
            CpfRuntimeControlPlane controlPlane,
            String controllerId,
            int leaseSeconds,
            int ackTimeoutSeconds) {
        this(
                repository,
                controlPlane,
                controllerId,
                leaseSeconds,
                ackTimeoutSeconds,
                Set.of(),
                3,
                1,
                3,
                30_000L);
    }

    public CpfRuntimeControlReconciler(
            CpfRuntimeControlPlaneRepository repository,
            CpfRuntimeControlPlane controlPlane,
            String controllerId,
            int leaseSeconds,
            int ackTimeoutSeconds,
            Set<String> autoRollbackAllowlist,
            int autoRollbackMaxAttempts,
            int autoRollbackMaxPerRun,
            int autoRollbackCircuitFailureThreshold,
            long autoRollbackCircuitOpenMillis) {
        this.repository = repository;
        this.controlPlane = controlPlane;
        this.controllerId = controllerId;
        this.leaseSeconds = Math.max(10, Math.min(300, leaseSeconds));
        this.ackTimeoutSeconds = Math.max(10, ackTimeoutSeconds);
        this.autoRollbackPolicy =
                new CpfRuntimeAutoRollbackPolicy(
                        autoRollbackAllowlist,
                        autoRollbackMaxAttempts,
                        autoRollbackMaxPerRun,
                        autoRollbackCircuitFailureThreshold,
                        autoRollbackCircuitOpenMillis);
    }

    @Scheduled(fixedDelayString = "${cpf.runtime.control.controller.reconcile-millis:5000}")
    public void reconcile() {
        long fencingToken = repository.acquireControllerLease(controllerId, leaseSeconds);
        if (fencingToken <= 0L) {
            return;
        }
        repository.reconcileController(controllerId, fencingToken, ackTimeoutSeconds);
        int dispatched = 0;
        for (Map<String, Object> candidate : repository.autoRollbackCandidates()) {
            if (dispatched >= autoRollbackPolicy.maxPerRun()) {
                break;
            }
            String changeId = String.valueOf(candidate.get("change_id"));
            String changeType = String.valueOf(candidate.get("change_type"));
            String operationId = "AUTO_ROLLBACK:" + changeId;
            var existingOperation = repository.findOperation(operationId);
            if (existingOperation.isPresent()) {
                String operationState = nullable(existingOperation.get().get("result_state"));
                String rollbackChangeId = nullable(existingOperation.get().get("entity_id"));
                if (rollbackChangeId == null || rollbackChangeId.isBlank()) {
                    rollbackChangeId = repository.findChange("operation_id", operationId)
                            .map(row -> nullable(row.get("change_id"))).orElse(null);
                }
                if (rollbackChangeId != null && !rollbackChangeId.isBlank()) {
                    repository.linkRollbackChange(changeId, rollbackChangeId, controllerId,
                            "Recovered automatic rollback operation");
                    if ("PROCESSING".equals(operationState)) {
                        repository.completeOperation(operationId, rollbackChangeId, "SUCCESS",
                                repository.json(Map.of("changeId", rollbackChangeId, "recovered", true)));
                    } else if (!"SUCCESS".equals(operationState)) {
                        // Terminal operation Ledger는 덮어쓰지 않습니다. 이미 생성된 rollback Change는
                        // 별도 lifecycle 정본이므로 관계를 복구하되 충돌 Evidence를 남깁니다.
                        recordBlockedOnce(changeId, "OPERATION_LEDGER_CONFLICT_" + operationState,
                                rollbackChangeId);
                    }
                    recordDispatchedOnce(changeId, rollbackChangeId, operationId);
                } else if (Set.of("FAILED", "UNKNOWN", "EXPIRED", "CANCELLED").contains(operationState)) {
                    recordBlockedOnce(changeId, "OPERATION_" + operationState, operationId);
                } else {
                    recordBlockedOnce(changeId, "INCOMPLETE_ROLLBACK_OPERATION", operationId);
                }
                continue;
            }
            int attempts = repository.autoRollbackEventCount(changeId, "AUTO_ROLLBACK_ATTEMPT");
            int recentFailures =
                    repository.recentAutoRollbackFailureCount(
                            changeType,
                            Instant.now()
                                    .minusMillis(autoRollbackPolicy.circuitOpenMillis()));
            CpfRuntimeAutoRollbackPolicy.Decision decision =
                    autoRollbackPolicy.decide(
                            changeType,
                            nullable(candidate.get("approval_id")),
                            nullable(candidate.get("break_glass_id")),
                            attempts,
                            recentFailures);
            if (!decision.allowed()) {
                recordBlockedOnce(changeId, decision.reason(), operationId);
                continue;
            }
            if (repository.acknowledgedTargets(changeId).isEmpty()) {
                recordBlockedOnce(changeId, "NO_ACKNOWLEDGED_TARGET", operationId);
                continue;
            }

            repository.appendAutoRollbackAudit(
                    changeId,
                    "AUTO_ROLLBACK_ATTEMPT",
                    controllerId,
                    "Automatic rollback attempt " + (attempts + 1),
                    operationId);
            dispatched++;
            try {
                var result =
                        controlPlane.rollback(
                                changeId,
                                operationId,
                                "CPF Runtime Controller automatic rollback",
                                "CPF_CONTROLLER");
                recordDispatchedOnce(changeId, result.changeId(), operationId);
            } catch (RuntimeException failure) {
                repository.appendAutoRollbackAudit(
                        changeId,
                        "AUTO_ROLLBACK_FAILED",
                        controllerId,
                        failure.getClass().getSimpleName(),
                        operationId);
                log.error("Runtime automatic rollback 실패. changeId={}", changeId, failure);
            }
        }
    }

    private void recordDispatchedOnce(
            String changeId, String rollbackChangeId, String operationId) {
        if (repository.autoRollbackEventCount(changeId, "AUTO_ROLLBACK_DISPATCHED") == 0) {
            repository.appendAutoRollbackAudit(
                    changeId,
                    "AUTO_ROLLBACK_DISPATCHED",
                    controllerId,
                    "Automatic rollback change created",
                    rollbackChangeId == null || rollbackChangeId.isBlank()
                            ? operationId
                            : rollbackChangeId);
        }
    }

    private void recordBlockedOnce(String changeId, String reason, String evidence) {
        String eventType = boundedAuditEventType("AUTO_ROLLBACK_BLOCKED_" + reason);
        if (repository.autoRollbackEventCount(changeId, eventType) == 0) {
            repository.appendAutoRollbackAudit(
                    changeId,
                    eventType,
                    controllerId,
                    reason,
                    evidence);
        }
    }


    static String boundedAuditEventType(String value) {
        String normalized=value==null?"":value.trim().toUpperCase(java.util.Locale.ROOT)
                .replaceAll("[^A-Z0-9_]+","_");
        if(normalized.isBlank()) normalized="AUTO_ROLLBACK_BLOCKED_UNKNOWN";
        if(normalized.length()<=60) return normalized;
        String suffix=CpfRuntimeCanonicalHash.sha256Hex(normalized).substring(0,8).toUpperCase(java.util.Locale.ROOT);
        return normalized.substring(0,51)+"_"+suffix;
    }

    private String nullable(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
