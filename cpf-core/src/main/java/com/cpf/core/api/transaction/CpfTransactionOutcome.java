package com.cpf.core.api.transaction;

/** 거래 실행과 복구에서 공통으로 사용하는 결과 상태입니다. */
public enum CpfTransactionOutcome {
    ACTIVE,
    PREPARED,
    COMMITTED,
    ROLLED_BACK,
    RETRYABLE_FAILURE,
    FAILED,
    HEURISTIC,
    IN_DOUBT,
    UNKNOWN,
    MANUAL_REVIEW
}
