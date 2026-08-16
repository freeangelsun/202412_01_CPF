package com.cpf.security.api.approval;
/** Security Runtime이 Owner 승인 시스템에 질의하는 기술중립 검증 요청입니다. */
public record CpfApprovalVerification(long approvalId, String action, int requiredApprovals,
                                      String actorId, String reason, String transactionId) {
    public CpfApprovalVerification {
        if (approvalId <= 0) throw new IllegalArgumentException("approvalId");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("action");
        if (requiredApprovals < 1) throw new IllegalArgumentException("requiredApprovals");
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId");
    }
}
