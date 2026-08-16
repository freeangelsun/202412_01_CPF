package com.cpf.platform.operations.api.state;

/** Typed result that never exposes provider exceptions. */
/** CpfStateTransitionResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfStateTransitionResult(Status status, CpfStateSnapshot snapshot, String message) {
    public CpfStateTransitionResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : message;
    }

    public boolean applied() {
        return status == Status.APPLIED || status == Status.IDEMPOTENT_REPLAY;
    }

    /** Status 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
