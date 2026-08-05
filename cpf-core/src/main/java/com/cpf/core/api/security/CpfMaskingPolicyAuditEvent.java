package com.cpf.core.api.security;

import java.time.Instant;

/** Sanitized audit event; command and principals are hashed before persistence. */
public record CpfMaskingPolicyAuditEvent(
        Phase phase,
        String commandIdHash,
        String commandHash,
        String actorHash,
        String approverHash,
        long beforeVersion,
        long afterVersion,
        String reason,
        String result,
        Instant occurredAt) {
    public CpfMaskingPolicyAuditEvent {
        if (phase == null || occurredAt == null) throw new IllegalArgumentException("audit fields are required");
        commandIdHash = hash(commandIdHash, "commandIdHash");
        commandHash = hash(commandHash, "commandHash");
        actorHash = hash(actorHash, "actorHash");
        approverHash = hash(approverHash, "approverHash");
        if (beforeVersion < 1L || afterVersion < 1L) throw new IllegalArgumentException("versions must be positive");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
        result = result == null ? "" : CpfSensitiveData.sanitizeAuditText(result);
    }

    private static String hash(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }

    public enum Phase { PREPARE, APPLIED, UNKNOWN }
}
