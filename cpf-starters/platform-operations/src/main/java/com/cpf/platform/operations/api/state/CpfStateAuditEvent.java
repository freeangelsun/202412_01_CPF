package com.cpf.platform.operations.api.state;

import com.cpf.security.api.CpfSensitiveData;
import java.time.Instant;
import java.util.Objects;

/** Sanitized immutable decision event for state transitions and replays. */
/** CpfStateAuditEvent 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfStateAuditEvent(
        String stateKeyHash,
        String operationIdHash,
        String actor,
        CpfOperationState beforeState,
        CpfOperationState requestedState,
        CpfOperationState resultingState,
        long beforeVersion,
        long resultingVersion,
        String decision,
        String reason,
        Instant decidedAt) {
    public CpfStateAuditEvent {
        stateKeyHash = sha256(stateKeyHash, "stateKeyHash");
        operationIdHash = sha256(operationIdHash, "operationIdHash");
        actor = CpfStateIdentifiers.actor(actor);
        requestedState = Objects.requireNonNull(requestedState, "requestedState");
        decision = CpfStateIdentifiers.identifier(decision, "decision", 64);
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
        decidedAt = Objects.requireNonNull(decidedAt, "decidedAt");
        if (beforeVersion < -1L || resultingVersion < -1L) {
            throw new IllegalArgumentException("state audit versions must be >= -1");
        }
    }

    private static String sha256(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }
}
