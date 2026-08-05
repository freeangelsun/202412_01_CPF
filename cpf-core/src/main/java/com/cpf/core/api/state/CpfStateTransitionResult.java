package com.cpf.core.api.state;

/** Typed result that never exposes provider exceptions. */
public record CpfStateTransitionResult(Status status, CpfStateSnapshot snapshot, String message) {
    public CpfStateTransitionResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : message;
    }

    public boolean applied() {
        return status == Status.APPLIED || status == Status.IDEMPOTENT_REPLAY;
    }

    public enum Status {
        APPLIED,
        IDEMPOTENT_REPLAY,
        NOT_FOUND,
        VERSION_CONFLICT,
        OPERATION_CONFLICT,
        INVALID_TRANSITION,
        RESOURCE_EXHAUSTED,
        UNKNOWN_RESULT,
        AUDIT_UNAVAILABLE,
        STORE_UNAVAILABLE
    }
}
