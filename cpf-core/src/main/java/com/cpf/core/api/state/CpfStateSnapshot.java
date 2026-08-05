package com.cpf.core.api.state;

import com.cpf.core.api.security.CpfSensitiveData;
import java.time.Instant;
import java.util.Objects;

/** Immutable optimistic-lock snapshot of one operation state. */
public record CpfStateSnapshot(
        String stateKey,
        CpfOperationState state,
        long version,
        String lastOperationId,
        String actor,
        String reason,
        Instant updatedAt) {

    public CpfStateSnapshot {
        stateKey = CpfStateIdentifiers.stateKey(stateKey);
        state = Objects.requireNonNull(state, "state");
        if (version < 0L) throw new IllegalArgumentException("version must be non-negative");
        lastOperationId = CpfStateIdentifiers.operationId(lastOperationId);
        actor = CpfStateIdentifiers.actor(actor);
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
    }
}
