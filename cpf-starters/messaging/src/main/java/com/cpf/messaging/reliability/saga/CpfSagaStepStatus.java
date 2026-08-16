package com.cpf.messaging.reliability.saga;

/** Saga Step의 정방향/보상 lifecycle 상태입니다. */
public enum CpfSagaStepStatus {
    RUNNING,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED
}
