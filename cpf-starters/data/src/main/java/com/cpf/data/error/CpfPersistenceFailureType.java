package com.cpf.data.error;

/** Persistence Provider가 공통으로 분류하는 실패 유형입니다. */
public enum CpfPersistenceFailureType {
    NOT_FOUND,
    OPTIMISTIC_LOCK,
    PESSIMISTIC_LOCK,
    TIMEOUT,
    CONSTRAINT,
    CONNECTION,
    QUERY,
    TRANSIENT,
    UNKNOWN
}
