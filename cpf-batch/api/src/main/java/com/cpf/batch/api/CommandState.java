package com.cpf.batch.api;

/** 결과 불명 거래를 성공으로 축약하지 않는다. */
public enum CommandState {
    REQUESTED, APPROVED, PLANNED, EXECUTING, VERIFYING,
    SUCCEEDED, FAILED, UNKNOWN_RESULT, RECONCILING,
    ROLLED_BACK, PARTIALLY_ROLLED_BACK
}
