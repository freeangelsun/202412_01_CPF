package com.cpf.platform.operations.observability.api.logging.policy;

import com.cpf.security.api.CpfSensitiveData;

/** Typed mutation result; UNKNOWN_RESULT requires querying and reconciling before retry. */
/** CpfLogPolicyVersionResult 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfLogPolicyVersionResult(
        Status status,
        CpfLogPolicyVersionSnapshot snapshot,
        String message) {
    public CpfLogPolicyVersionResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : CpfSensitiveData.sanitizeAuditText(message);
    }

    /** Status 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public enum Status {
        APPLIED,
        IDEMPOTENT_REPLAY,
        VERSION_CONFLICT,
        COMMAND_CONFLICT,
        TARGET_NOT_FOUND,
        TARGET_VERSION_NOT_FOUND,
        APPROVAL_REQUIRED,
        APPROVAL_INVALID,
        AUDIT_UNAVAILABLE,
        STORE_UNAVAILABLE,
        RESOURCE_EXHAUSTED,
        RECONCILIATION_REQUIRED,
        UNKNOWN_RESULT
    }
}
