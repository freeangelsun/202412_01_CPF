package com.cpf.admin.opr.dto;

/** Ghost·Unknown Result 운영 판정 요청입니다. */
public record AdmBatchGhostActionRequest(
        String actionType,
        String reason,
        String approvalRequestId,
        Long expectedVersion,
        String idempotencyKey) {
    public AdmBatchGhostActionRequest(String actionType, String reason) {
        this(actionType, reason, null, null, null);
    }
}
