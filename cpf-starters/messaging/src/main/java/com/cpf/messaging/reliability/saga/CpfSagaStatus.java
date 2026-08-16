package com.cpf.messaging.reliability.saga;

/** Saga durable lifecycle 상태입니다. */
public enum CpfSagaStatus {
    STARTED,
    RUNNING,
    COMPLETED,
    FAILED,
    UNKNOWN,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED,
    MANUAL_REVIEW,
    MANUAL_INTERVENTION_REQUIRED,
    MANUALLY_RESOLVED
}
