package com.cpf.admin.approval.api;

/**
 * 승인 완료 후 실제 Owner Module에 전달할 위험조치 Command의 불변 Snapshot입니다.
 *
 * <p>민감 Payload 원문을 이 Contract에 강제하지 않습니다. Approval Request에는 마스킹된 Snapshot과
 * SHA-256 hash를 보존하고 실제 Secret/Credential은 Secret Provider 등 안전한 경계를 통해 해석해야 합니다.</p>
 *
 * @param approvalRequestId ADM 승인 요청 ID
 * @param commandRequestId Owner Command 멱등 요청 ID
 * @param actionType 위험조치 유형
 * @param ownerModule 실제 명령 Owner Module
 * @param ownerCommand Owner가 노출한 Command 이름
 * @param targetType 대상 유형
 * @param targetId 대상 ID
 * @param payloadHash 승인한 Payload SHA-256
 * @param transactionGlobalId CPF 전역 거래 ID
 */
public record AdmApprovedOperationCommand(
        long approvalRequestId,
        String commandRequestId,
        String actionType,
        String ownerModule,
        String ownerCommand,
        String targetType,
        String targetId,
        String payloadHash,
        String transactionGlobalId) {

    public AdmApprovedOperationCommand {
        requireText(commandRequestId, "commandRequestId");
        requireText(actionType, "actionType");
        requireText(ownerModule, "ownerModule");
        requireText(ownerCommand, "ownerCommand");
        requireText(targetType, "targetType");
        requireText(targetId, "targetId");
        if (payloadHash == null || !payloadHash.matches("[0-9a-fA-F]{64}")) {
            throw new IllegalArgumentException("payloadHash는 SHA-256 hex 64자리여야 합니다.");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "는 필수입니다.");
        }
    }
}
