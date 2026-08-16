package com.cpf.messaging.reliability.saga;

import java.util.Optional;

/** Saga 실행/Step/수동복구 상태의 durable 저장 Port입니다. */
public interface CpfSagaStateStore {
    CpfSagaSnapshot create(CpfSagaContext context);
    Optional<CpfSagaSnapshot> find(String sagaId);
    void markSaga(String sagaId, CpfSagaStatus status, String errorMessage);
    void markStep(String sagaId, int stepNo, String stepId, CpfSagaStepStatus status,
                  CpfSagaStepResult result, String errorMessage, boolean compensationAttempt);
    void auditManualAction(String sagaId, String actionType, String operatorId, String reason,
                           String beforeStatus, String afterStatus);
}
