package com.cpf.core.api.security;

/** Typed result; UNKNOWN_RESULT means the durable version may have changed and must be queried. */
public record CpfMaskingPolicyResult(
        Status status,
        CpfMaskingPolicySnapshot snapshot,
        String message) {
    public CpfMaskingPolicyResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : CpfSensitiveData.sanitizeAuditText(message);
    }

    public enum Status {
        APPLIED,
        IDEMPOTENT_REPLAY,
        VERSION_CONFLICT,
        COMMAND_CONFLICT,
        TARGET_VERSION_NOT_FOUND,
        APPROVAL_REQUIRED,
        AUDIT_UNAVAILABLE,
        STORE_UNAVAILABLE,
        RESOURCE_EXHAUSTED,
        UNKNOWN_RESULT
    }
}
