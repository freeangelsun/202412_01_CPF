package com.cpf.core.api.security;

import java.util.regex.Pattern;

/** Creates a new policy version from a prior version; versions are never decremented. */
public record CpfMaskingPolicyRollbackCommand(
        String commandId,
        long expectedVersion,
        long targetVersion,
        String actor,
        String reason,
        CpfMaskingPolicyApproval approval) {
    private static final Pattern ID = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");

    public CpfMaskingPolicyRollbackCommand {
        commandId = identifier(commandId, "commandId");
        if (expectedVersion < 1L || targetVersion < 1L) {
            throw new IllegalArgumentException("versions must be positive");
        }
        if (targetVersion >= expectedVersion) {
            throw new IllegalArgumentException("targetVersion must be older than expectedVersion");
        }
        actor = identifier(actor, "actor");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }

    public String commandHash() {
        return CpfMaskingPolicyUpdateCommand.sha256(
                "ROLLBACK|" + commandId + "|" + expectedVersion + "|" + targetVersion + "|" + actor + "|" + reason);
    }

    private static String identifier(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (!ID.matcher(normalized).matches()) throw new IllegalArgumentException(field + " is invalid");
        return normalized;
    }
}
