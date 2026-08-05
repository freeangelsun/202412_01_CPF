package com.cpf.core.api.logging.policy;

import com.cpf.core.api.security.CpfSensitiveData;
import java.util.Objects;
import java.util.regex.Pattern;

/** Re-applies one UNKNOWN/DRAFT version and atomically promotes it to ACTIVE. */
public record CpfLogPolicyVersionReconcileCommand(
        String commandId,
        LogPolicyTargetType targetType,
        String targetId,
        long expectedVersion,
        String actor,
        String reason,
        CpfLogPolicyVersionApproval approval) {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");

    public CpfLogPolicyVersionReconcileCommand {
        commandId = identifier(commandId, "commandId");
        targetType = Objects.requireNonNull(targetType, "targetType");
        targetId = LogPolicyDecision.normalizeTargetId(targetId);
        if (expectedVersion < 1L) throw new IllegalArgumentException("expectedVersion must be positive");
        actor = identifier(actor, "actor");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }

    public String commandHash() {
        return CpfLogPolicyVersionUpdateCommand.sha256("RECONCILE|" + commandId + '|'
                + targetType.code() + '|' + targetId + '|' + expectedVersion + '|' + actor + '|' + reason);
    }

    private static String identifier(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (!IDENTIFIER.matcher(normalized).matches()
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }
}
