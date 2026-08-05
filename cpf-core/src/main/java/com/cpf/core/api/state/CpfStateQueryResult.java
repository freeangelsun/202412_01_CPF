package com.cpf.core.api.state;

/** Query result preserving the distinction between absence and provider failure. */
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

    public enum Status {
        FOUND,
        NOT_FOUND,
        STORE_UNAVAILABLE
    }
}
