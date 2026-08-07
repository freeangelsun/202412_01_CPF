package com.cpf.admin.approval.service;

import com.cpf.admin.approval.repository.AdmApprovalRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Cluster-safe safety net for process loss while an approved Owner command is RUNNING.
 *
 * <p>The worker never replays a mutation. It only converts an expired RUNNING reservation to
 * UNKNOWN so that the existing observation-only reconcile path can query the Owner state.</p>
 */
@Component
public final class AdmApprovalRecoveryWorker {
    private static final String SYSTEM_ACTOR="SYSTEM_APPROVAL_RECOVERY";
    private final AdmApprovalRepository repository;

    public AdmApprovalRecoveryWorker(AdmApprovalRepository repository) {
        this.repository=repository;
    }

    @Scheduled(
            initialDelayString="${cpf.adm.approval.recovery-initial-delay-ms:5000}",
            fixedDelayString="${cpf.adm.approval.recovery-sweep-ms:30000}")
    public void recoverExpiredExecutions() {
        repository.sweepExpiredExecutions(Instant.now(),100,SYSTEM_ACTOR);
    }
}
