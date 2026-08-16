package com.cpf.admin.opr.dto;

/** 만료 Lock 강제 해제 요청입니다. */
public record AdmBatchLockReleaseRequest(
        String lockKey,
        String reason,
        String approvalRequestId,
        Long expectedVersion,
        String idempotencyKey) {
    public AdmBatchLockReleaseRequest(String lockKey, String reason) {
        this(lockKey, reason, null, null, null);
    }
}
