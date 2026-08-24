package com.cpf.batch.api;

/**
 * Center-Cut 실행의 Canonical lifecycle 상태입니다.
 * <p>{@code UNKNOWN_RESULT}는 결과를 임의 재실행하지 않고 별도 확인/Reconcile이 필요한 상태입니다.
 */
public enum CenterCutExecutionState {
    CREATED, TARGETING, TARGET_READY, STARTING, RUNNING, PAUSED, DRAINING,
    CANCELLED, COMPLETED, FAILED, UNKNOWN_RESULT
}
