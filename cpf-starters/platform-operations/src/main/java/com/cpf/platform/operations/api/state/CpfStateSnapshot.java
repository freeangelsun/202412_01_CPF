package com.cpf.platform.operations.api.state;

import com.cpf.security.api.CpfSensitiveData;
import java.time.Instant;
import java.util.Objects;

/** Immutable optimistic-lock snapshot of one operation state. */
/** CpfStateSnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
