package com.cpf.batch.api;

/** CPF Control Plane 실행 상태 정본입니다. Spring Batch 상태와 분리해 결과 불명·대사를 표현합니다. */
public enum BatchControlState {
    RESERVED,
    STARTING,
    STARTED,
    STOPPING,
    STOPPED,
    COMPLETED,
    FAILED,
    UNKNOWN_RESULT,
    ABANDONING,
    ABANDONED,
    REJECTED;

    public boolean terminal() {
        return this == STOPPED || this == COMPLETED || this == FAILED || this == ABANDONED || this == REJECTED;
    }
}
