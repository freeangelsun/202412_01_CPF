package com.cpf.reference.edu.runtime.model;
public enum EduExecutionState {
    REQUESTED, VALIDATED, WAITING_APPROVAL, CLAIMED, IN_PROGRESS, WAITING_EXTERNAL,
    UNKNOWN_RESULT, PARTIAL_SUCCESS, SUCCEEDED, FAILED_RETRYABLE, FAILED_PERMANENT,
    RECONCILING, COMPENSATING, COMPENSATED, CANCELLED, ROLLED_BACK;
    public boolean terminal() {
        return this == SUCCEEDED || this == FAILED_PERMANENT || this == COMPENSATED
                || this == CANCELLED || this == ROLLED_BACK;
    }
}
