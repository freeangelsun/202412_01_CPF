package com.cpf.education.operations.runtime.model;
/** EduExecutionState 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum EduExecutionState {
    REQUESTED, VALIDATED, WAITING_APPROVAL, CLAIMED, IN_PROGRESS, WAITING_EXTERNAL,
    UNKNOWN_RESULT, PARTIAL_SUCCESS, SUCCEEDED, FAILED_RETRYABLE, FAILED_PERMANENT,
    RECONCILING, COMPENSATING, COMPENSATED, CANCELLED, ROLLED_BACK;
    public boolean terminal() {
        return this == SUCCEEDED || this == FAILED_PERMANENT || this == COMPENSATED
                || this == CANCELLED || this == ROLLED_BACK;
    }
}
