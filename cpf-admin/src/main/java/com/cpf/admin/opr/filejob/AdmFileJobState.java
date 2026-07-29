package com.cpf.admin.opr.filejob;

/** 대량파일 Job의 명시적 상태표입니다. */
public enum AdmFileJobState {
    RECEIVED, VALIDATING, VALIDATED, READY_TO_APPLY, APPLYING, COMPLETED,
    PARTIAL_FAILED, FAILED, UNKNOWN_RESULT, CANCELLED, ROLLING_BACK, ROLLED_BACK, EXPIRED;

    public boolean terminal() {
        return this == COMPLETED || this == PARTIAL_FAILED || this == FAILED
                || this == UNKNOWN_RESULT || this == CANCELLED || this == ROLLED_BACK || this == EXPIRED;
    }

    public boolean recoverableWorkState() {
        return this == RECEIVED || this == VALIDATING || this == APPLYING || this == ROLLING_BACK;
    }
}
