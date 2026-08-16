package com.cpf.platform.operations.api.state;

/** Canonical lifecycle state for long-running or externally observable operations. */
/** CpfOperationState 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum CpfOperationState {
    NEW(false),
    RUNNING(false),
    UNKNOWN(false),
    SUCCEEDED(true),
    FAILED(true),
    CANCELLED(true);

    private final boolean terminal;

    CpfOperationState(boolean terminal) {
        this.terminal = terminal;
    }

    /** terminal 작업을 CPF 표준 계약에 따라 수행한다. */
    public boolean terminal() {
        return terminal;
    }
}
