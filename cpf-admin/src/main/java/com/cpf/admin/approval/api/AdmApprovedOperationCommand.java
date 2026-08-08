package com.cpf.admin.approval.api;

/** 승인 완료 후 실제 Owner Module에 전달할 불변 위험조치 Snapshot입니다. */
public record AdmApprovedOperationCommand(
        long approvalRequestId,
        String commandRequestId,
        String actionType,
        String ownerModule,
        String ownerCommand,
        String targetType,
        String targetId,
        String payloadHash,
        String payloadSnapshot,
        String requestedBy,
        String approvedBy,
        String reason,
        String transactionId,
        String leaseOwner,
        long fenceToken) {

    public AdmApprovedOperationCommand {
        if (approvalRequestId <= 0) throw new IllegalArgumentException("approvalRequestId는 양수여야 합니다.");
        commandRequestId = requireText(commandRequestId, "commandRequestId");
        actionType = requireText(actionType, "actionType");
        ownerModule = requireText(ownerModule, "ownerModule");
        ownerCommand = requireText(ownerCommand, "ownerCommand");
        targetType = requireText(targetType, "targetType");
        targetId = requireText(targetId, "targetId");
        requestedBy = requireText(requestedBy, "requestedBy");
        approvedBy = requireText(approvedBy, "approvedBy");
        reason = requireText(reason, "reason");
        transactionId = requireText(transactionId, "transactionId");
        leaseOwner = requireText(leaseOwner, "leaseOwner");
        if (fenceToken < 0) throw new IllegalArgumentException("fenceToken은 0 이상이어야 합니다.");
        payloadSnapshot = payloadSnapshot == null || payloadSnapshot.isBlank() ? "{}" : payloadSnapshot;
        if (payloadHash == null || !payloadHash.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("payloadHash는 SHA-256 hex 64자리여야 합니다.");
        }
    }

    /** 기존 Owner Adapter Source 호환용 생성자입니다. */
    public AdmApprovedOperationCommand(
            long approvalRequestId, String commandRequestId, String actionType,
            String ownerModule, String ownerCommand, String targetType, String targetId,
            String payloadHash, String requestedBy, String approvedBy,
            String reason, String transactionId) {
        this(approvalRequestId, commandRequestId, actionType, ownerModule, ownerCommand,
                targetType, targetId, payloadHash, "{}", requestedBy, approvedBy, reason, transactionId,
                "LEGACY-UNFENCED", 0L);
    }

    /** Source compatibility for pre-fence callers. Product execution paths must pass the reserved lease/fence. */
    public AdmApprovedOperationCommand(
            long approvalRequestId, String commandRequestId, String actionType,
            String ownerModule, String ownerCommand, String targetType, String targetId,
            String payloadHash, String payloadSnapshot, String requestedBy, String approvedBy,
            String reason, String transactionId) {
        this(approvalRequestId, commandRequestId, actionType, ownerModule, ownerCommand,
                targetType, targetId, payloadHash, payloadSnapshot, requestedBy, approvedBy, reason, transactionId,
                "LEGACY-UNFENCED", 0L);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "는 필수입니다.");
        }
        return value.trim();
    }
}
