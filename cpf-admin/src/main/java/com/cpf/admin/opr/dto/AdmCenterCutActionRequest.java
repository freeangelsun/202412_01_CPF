package com.cpf.admin.opr.dto;

/** 승인된 Center-Cut execution-scope 복구 요청입니다. */
public record AdmCenterCutActionRequest(
        String reason,
        String approvalRequestId,
        String idempotencyKey) {
    public AdmCenterCutActionRequest {
        reason = required(reason, "reason");
        approvalRequestId = required(approvalRequestId, "approvalRequestId");
        idempotencyKey = required(idempotencyKey, "idempotencyKey");
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
