package com.cpf.batch.control.deploy;

import java.time.Instant;
import java.util.List;

/** 배포 결과 불명 상태를 durable execution/instance ledger와 cell lock owner로 대사한 결과입니다. */
public record DeploymentReconciliation(
        String deploymentId,
        String cellId,
        String previousState,
        String reconciliationState,
        String lockOwner,
        int sideEffectCount,
        List<SideEffect> sideEffects,
        String detail,
        Instant reconciledAt) {
    public record SideEffect(int sequence, String instanceId, String stage, String state, String message) {}
}
