package com.cpf.platform.operations.observability.api.logging.policy;

import java.time.Duration;
import java.time.Instant;

/** Scoped, short-lived separation-of-duties approval for one log-policy command hash. */
/** CpfLogPolicyVersionApproval 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfLogPolicyVersionApproval(
        String commandHash,
        String approver,
        Instant approvedAt,
        Instant expiresAt) {
    private static final Duration MAXIMUM_LIFETIME = Duration.ofMinutes(30);
    public CpfLogPolicyVersionApproval {
        if (commandHash == null || !commandHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandHash must be lowercase SHA-256");
        }
        approver = required(approver, "approver", 128);
        if (approvedAt == null || expiresAt == null || !expiresAt.isAfter(approvedAt)) {
            throw new IllegalArgumentException("approval time range is invalid");
        }
        if (Duration.between(approvedAt, expiresAt).compareTo(MAXIMUM_LIFETIME) > 0) {
            throw new IllegalArgumentException("approval lifetime must not exceed 30 minutes");
        }
    }
    private static String required(String value, String field, int maximumLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > maximumLength || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }
}
