package com.cpf.core.api.logging.policy;

import com.cpf.core.api.security.CpfSensitiveData;
import java.time.Instant;
import java.util.Objects;

/** Immutable versioned log-policy snapshot without exposing provider implementation types. */
public record CpfLogPolicyVersionSnapshot(
        LogPolicyTargetType targetType,
        String targetId,
        long version,
        Status status,
        LogPolicyDecision decision,
        Instant updatedAt,
        String updatedBy,
        String reason) {
    public CpfLogPolicyVersionSnapshot {
        targetType = Objects.requireNonNull(targetType, "targetType");
        targetId = LogPolicyDecision.normalizeTargetId(targetId);
        if (version < 1L) throw new IllegalArgumentException("version must be positive");
        status = Objects.requireNonNull(status, "status");
        decision = Objects.requireNonNull(decision, "decision");
        if (!targetType.code().equals(LogPolicyTargetType.fromCode(decision.targetType()).code())
                || !targetId.equals(LogPolicyDecision.normalizeTargetId(decision.targetId()))) {
            throw new IllegalArgumentException("snapshot target and decision target must match");
        }
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        updatedBy = required(updatedBy, "updatedBy", 128);
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }
    public enum Status { DRAFT, ACTIVE, INACTIVE, FAILED, UNKNOWN }
    private static String required(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > maximumLength || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }
}
