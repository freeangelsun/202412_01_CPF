package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlPlane;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeCapabilityCatalog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Controller leader lease를 소유한 한 인스턴스만 전역 Runtime 상태 전이를 수행합니다. */
public class CpfRuntimeControlReconciler {
    private static final Logger log = LoggerFactory.getLogger(CpfRuntimeControlReconciler.class);
    private final CpfRuntimeControlPlaneRepository repository;
    private final CpfRuntimeControlPlane controlPlane;
    private final String controllerId;
    private final int leaseSeconds;
    private final int ackTimeoutSeconds;
    private final Set<String> selfHealingAllowlist;
    private final int selfHealingRateLimitPerMinute;
    private final int selfHealingCircuitFailureThreshold;
    private final int selfHealingCircuitWindowSeconds;

    public CpfRuntimeControlReconciler(CpfRuntimeControlPlaneRepository repository,
                                       CpfRuntimeControlPlane controlPlane,
                                       String controllerId,
                                       int leaseSeconds,
                                       int ackTimeoutSeconds,
                                       String selfHealingAllowedChangeTypes,
                                       int selfHealingRateLimitPerMinute,
                                       int selfHealingCircuitFailureThreshold,
                                       int selfHealingCircuitWindowSeconds) {
        this.repository = repository;
        this.controlPlane = controlPlane;
        this.controllerId = controllerId;
        this.leaseSeconds = Math.max(10, Math.min(300, leaseSeconds));
        this.ackTimeoutSeconds = Math.max(10, ackTimeoutSeconds);
        this.selfHealingAllowlist = Arrays.stream(selfHealingAllowedChangeTypes == null ? new String[0] : selfHealingAllowedChangeTypes.split(","))
                .map(String::trim).filter(v -> !v.isBlank()).map(v -> v.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
        this.selfHealingRateLimitPerMinute = Math.max(1, Math.min(60, selfHealingRateLimitPerMinute));
        this.selfHealingCircuitFailureThreshold = Math.max(1, Math.min(20, selfHealingCircuitFailureThreshold));
        this.selfHealingCircuitWindowSeconds = Math.max(60, selfHealingCircuitWindowSeconds);
    }

    @Scheduled(fixedDelayString = "${cpf.runtime.control.controller.reconcile-millis:5000}")
    @Transactional(transactionManager = "cpfTransactionManager")
    public void reconcile() {
        long fencingToken = repository.acquireControllerLease(controllerId, leaseSeconds);
        if (fencingToken <= 0L) return;
        repository.reconcileController(controllerId, fencingToken, ackTimeoutSeconds);
        if (selfHealingAllowlist.isEmpty()) return;
        if (repository.selfHealingCircuitOpen(selfHealingCircuitFailureThreshold,
                Instant.now().minusSeconds(selfHealingCircuitWindowSeconds))) {
            log.error("Runtime self-healing circuit가 열려 automatic rollback을 중지합니다.");
            return;
        }
        for (Map<String,Object> candidate : repository.autoRollbackCandidates()) {
            String changeId = String.valueOf(candidate.get("change_id"));
            String changeType = String.valueOf(candidate.get("change_type")).trim().toUpperCase(Locale.ROOT);
            String capability = CpfRuntimeCapabilityCatalog.resolve(changeType)
                    .map(CpfRuntimeCapabilityCatalog.Capability::code).orElse(changeType);
            if (!selfHealingAllowlist.contains(capability) && !selfHealingAllowlist.contains(changeType)) continue;
            String operationId = "AUTO_ROLLBACK:" + changeId;
            if (repository.findOperation(operationId).isPresent()) continue; // change당 최대 1회 시도
            if (repository.acknowledgedTargets(changeId).isEmpty()) continue;
            try {
                repository.consumeRateLimit("CPF_SELF_HEALING", selfHealingRateLimitPerMinute);
                controlPlane.rollback(changeId, operationId,
                        "CPF_SELF_HEALING: approved change automatic rollback", "CPF_CONTROLLER");
            } catch (RuntimeException failure) {
                log.error("Runtime automatic rollback 실패. changeId={}", changeId, failure);
            }
        }
    }
}
