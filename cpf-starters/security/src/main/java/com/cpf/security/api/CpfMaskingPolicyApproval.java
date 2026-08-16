package com.cpf.security.api;

import java.time.Instant;

/** Scoped, short-lived approval for a single masking policy command hash. */
/** CpfMaskingPolicyApproval 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfMaskingPolicyApproval(
        String commandHash,
        String approver,
        Instant approvedAt,
        Instant expiresAt) {
    public CpfMaskingPolicyApproval {
        commandHash = required(commandHash, "commandHash", 64);
        if (!commandHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("commandHash must be lowercase SHA-256");
        }
        approver = required(approver, "approver", 128);
        if (approvedAt == null || expiresAt == null || !expiresAt.isAfter(approvedAt)) {
            throw new IllegalArgumentException("approval time range is invalid");
        }
    }

    private static String required(String value, String field, int max) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (normalized.length() > max) throw new IllegalArgumentException(field + " is too long");
        return normalized;
    }
}
