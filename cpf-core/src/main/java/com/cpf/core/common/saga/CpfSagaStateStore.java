package com.cpf.core.common.saga;

import java.util.Optional;

/** Saga durable state SPI. 구현은 상태 전이를 원자적으로 기록해야 합니다. */
public interface CpfSagaStateStore {
    CpfSagaSnapshot create(CpfSagaContext context);
    Optional<CpfSagaSnapshot> find(String sagaId);
    void markSaga(String sagaId,CpfSagaStatus status,String errorMessage);
    void markStep(String sagaId,int stepNo,String stepId,CpfSagaStepStatus status,CpfSagaStepResult result,String errorMessage,boolean compensationAttempt);
    void auditManualAction(String sagaId,String actionType,String operatorId,String reason,String beforeStatus,String afterStatus);
}
