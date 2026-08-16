package com.cpf.platform.operations.api.state;

/** Query result preserving the distinction between absence and provider failure. */
/** CpfStateQueryResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfStateQueryResult(Status status, CpfStateSnapshot snapshot) {
    public CpfStateQueryResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        if (status == Status.FOUND && snapshot == null) {
            throw new IllegalArgumentException("FOUND requires a snapshot");
        }
        if (status != Status.FOUND && snapshot != null) {
            throw new IllegalArgumentException("non-FOUND result must not contain a snapshot");
        }
    }

    /** Status 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Status {
        FOUND,
        NOT_FOUND,
        STORE_UNAVAILABLE
    }
}
