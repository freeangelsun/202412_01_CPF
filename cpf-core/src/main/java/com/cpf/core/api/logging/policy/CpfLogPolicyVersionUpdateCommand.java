package com.cpf.core.api.logging.policy;

import com.cpf.core.api.security.CpfSensitiveData;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.regex.Pattern;

/** Optimistic, idempotent command that creates or replaces an active log-policy version. */
public record CpfLogPolicyVersionUpdateCommand(
        String commandId,
        long expectedVersion,
        LogPolicyDecision decision,
        String actor,
        String reason,
        CpfLogPolicyVersionApproval approval) {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");
    public CpfLogPolicyVersionUpdateCommand {
        commandId = identifier(commandId, "commandId");
        if (expectedVersion < 1L) throw new IllegalArgumentException("expectedVersion must be positive");
        decision = Objects.requireNonNull(decision, "decision");
        actor = identifier(actor, "actor");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }
    public LogPolicyTargetType targetType() { return LogPolicyTargetType.fromCode(decision.targetType()); }
    public String targetId() { return LogPolicyDecision.normalizeTargetId(decision.targetId()); }
    public String commandHash() {
        return sha256("UPDATE|" + commandId + '|' + expectedVersion + '|'
                + targetType().code() + '|' + targetId() + '|' + decision.policyChecksum()
                + '|' + actor + '|' + reason);
    }
    static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException unavailable) {
            throw new IllegalStateException("SHA-256 unavailable", unavailable);
        }
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
