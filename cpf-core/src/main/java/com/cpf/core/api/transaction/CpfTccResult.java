package com.cpf.core.api.transaction;

/** TCC 호출 결과입니다. UNKNOWN은 성공으로 간주하지 않고 Reconcile 대상입니다. */
public enum CpfTccResult {
    APPLIED,
    ALREADY_APPLIED,
    EMPTY_ROLLBACK,
    HANGING_REJECTED,
    RETRYABLE_FAILURE,
    FAILED,
    UNKNOWN,
    MANUAL_REVIEW
}
