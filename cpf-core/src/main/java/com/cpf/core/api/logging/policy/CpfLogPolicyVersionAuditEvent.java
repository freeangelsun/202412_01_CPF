package com.cpf.core.api.logging.policy;

import com.cpf.core.api.security.CpfSensitiveData;
import java.time.Instant;

/** Append-only audit event containing hashes instead of raw actor, target and command identifiers. */
public record CpfLogPolicyVersionAuditEvent(
        Phase phase,
        String commandIdHash,
        String commandHash,
        String targetHash,
        String actorHash,
        String approverHash,
        long beforeVersion,
        long afterVersion,
        CpfLogPolicyVersionSnapshot.Status beforeStatus,
        CpfLogPolicyVersionSnapshot.Status afterStatus,
        String reason,
        String result,
        Instant occurredAt) {
    public CpfLogPolicyVersionAuditEvent {
        if (phase == null || beforeStatus == null || afterStatus == null || occurredAt == null) {
            throw new IllegalArgumentException("phase, statuses and occurredAt are required");
        }
        commandIdHash = hash(commandIdHash, "commandIdHash");
        commandHash = hash(commandHash, "commandHash");
        targetHash = hash(targetHash, "targetHash");
        actorHash = hash(actorHash, "actorHash");
        approverHash = hash(approverHash, "approverHash");
        if (beforeVersion < 0L || afterVersion < 0L) throw new IllegalArgumentException("versions must be non-negative");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
        result = CpfSensitiveData.sanitizeAuditText(result == null ? "" : result);
    }
    public enum Phase { PREPARE, APPLIED, REJECTED, UNKNOWN }
    private static String hash(String value, String field) {
        if (value == null || !value.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(field + " must be lowercase SHA-256");
        }
        return value;
    }
}
