package com.cpf.security.api;

/** Typed result; UNKNOWN_RESULT means the durable version may have changed and must be queried. */
/** CpfMaskingPolicyResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfMaskingPolicyResult(
        Status status,
        CpfMaskingPolicySnapshot snapshot,
        String message) {
    public CpfMaskingPolicyResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : CpfSensitiveData.sanitizeAuditText(message);
    }

    /** Status 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
