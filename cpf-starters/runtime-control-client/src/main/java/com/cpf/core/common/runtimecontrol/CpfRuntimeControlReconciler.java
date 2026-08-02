package com.cpf.core.common.runtimecontrol;

import com.cpf.core.api.runtimecontrol.CpfRuntimeControlPlane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/** Controller leader lease를 소유한 한 인스턴스만 전역 Runtime 상태 전이를 수행합니다. */
public class CpfRuntimeControlReconciler {
    private static final Logger log = LoggerFactory.getLogger(CpfRuntimeControlReconciler.class);
    private final CpfRuntimeControlPlaneRepository repository;
    private final CpfRuntimeControlPlane controlPlane;
    private final String controllerId;
    private final int leaseSeconds;
    private final int ackTimeoutSeconds;

    public CpfRuntimeControlReconciler(CpfRuntimeControlPlaneRepository repository,
                                       CpfRuntimeControlPlane controlPlane,
                                       String controllerId,
                                       int leaseSeconds,
                                       int ackTimeoutSeconds) {
        this.repository = repository;
        this.controlPlane = controlPlane;
        this.controllerId = controllerId;
        this.leaseSeconds = Math.max(10, Math.min(300, leaseSeconds));
        this.ackTimeoutSeconds = Math.max(10, ackTimeoutSeconds);
    }

    @Scheduled(fixedDelayString = "${cpf.runtime.control.controller.reconcile-millis:5000}")
    @Transactional(transactionManager = "cpfTransactionManager")
    public void reconcile() {
        long fencingToken = repository.acquireControllerLease(controllerId, leaseSeconds);
        if (fencingToken <= 0L) return;
        repository.reconcileController(controllerId, fencingToken, ackTimeoutSeconds);
        for (Map<String,Object> candidate : repository.autoRollbackCandidates()) {
            String changeId = String.valueOf(candidate.get("change_id"));
            String operationId = "AUTO_ROLLBACK:" + changeId;
            if (repository.findOperation(operationId).isPresent()) continue;
            if (repository.acknowledgedTargets(changeId).isEmpty()) continue;
            try {
                controlPlane.rollback(changeId, operationId,
                        "CPF Runtime Controller automatic rollback", "CPF_CONTROLLER");
            } catch (RuntimeException failure) {
                log.error("Runtime automatic rollback 실패. changeId={}", changeId, failure);
            }
        }
    }
}
