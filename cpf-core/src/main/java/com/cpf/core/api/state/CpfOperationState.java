package com.cpf.core.api.state;

/** Canonical lifecycle state for long-running or externally observable operations. */
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

    public boolean terminal() {
        return terminal;
    }
}
