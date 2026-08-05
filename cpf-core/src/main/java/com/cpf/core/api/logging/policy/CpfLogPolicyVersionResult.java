package com.cpf.core.api.logging.policy;

import com.cpf.core.api.security.CpfSensitiveData;

/** Typed mutation result; UNKNOWN_RESULT requires querying and reconciling before retry. */
public record CpfLogPolicyVersionResult(
        Status status,
        CpfLogPolicyVersionSnapshot snapshot,
        String message) {
    public CpfLogPolicyVersionResult {
        if (status == null) throw new IllegalArgumentException("status is required");
        message = message == null ? "" : CpfSensitiveData.sanitizeAuditText(message);
    }

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
