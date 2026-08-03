package com.cpf.core.api.batch;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

/**
 * 승인·멱등·낙관적 잠금을 하나로 묶은 BAT 위험조치 Public Command 계약입니다.
 *
 * <p>ADM 승인 요청의 {@code command_payload_hash}, BAT Owner 멱등 Ledger의
 * {@code request_hash}, Remote 호출 payload가 모두 이 객체의 {@link #fingerprint()}를
 * 사용해야 합니다. 일부 필드만 전달하거나 Body에서 행위자를 덮어쓰는 구현은 허용하지 않습니다.</p>
 */
public record CpfBatchRiskCommand(
        String operation,
        String targetType,
        String targetId,
        String actionType,
        String requestUser,
        String reason,
        String approvalRequestId,
        String idempotencyKey,
        Long expectedVersion,
        String payload) {

    public CpfBatchRiskCommand {
        operation = required(operation, "operation");
        targetType = required(targetType, "targetType");
        targetId = required(targetId, "targetId");
        actionType = required(actionType, "actionType").toUpperCase(Locale.ROOT);
        requestUser = required(requestUser, "requestUser");
        reason = required(reason, "reason");
        approvalRequestId = required(approvalRequestId, "approvalRequestId");
        idempotencyKey = required(idempotencyKey, "idempotencyKey");
        if (idempotencyKey.length() > 120) {
            throw new IllegalArgumentException("idempotencyKey must be at most 120 characters");
        }
        if (expectedVersion != null && expectedVersion < 0) {
            throw new IllegalArgumentException("expectedVersion must be non-negative");
        }
        payload = payload == null ? "" : payload;
    }

    public long requiredExpectedVersion() {
        if (expectedVersion == null) {
            throw new IllegalArgumentException("expectedVersion is required for " + operation);
        }
        return expectedVersion;
    }

    /** 길이 접두어 Canonical 문자열의 SHA-256. 구분자 충돌과 JSON key 순서 차이를 제거합니다. */
    public String fingerprint() {
        String canonical = field(operation)
                + field(targetType)
                + field(targetId)
                + field(actionType)
                + field(requestUser)
                + field(reason)
                + field(approvalRequestId)
                + field(idempotencyKey)
                + field(expectedVersion == null ? "" : String.valueOf(expectedVersion))
                + field(payload);
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is not available", impossible);
        }
    }

    public void assertOperation(String expectedOperation, String expectedTargetType, String expectedTargetId) {
        if (!operation.equals(required(expectedOperation, "expectedOperation"))
                || !targetType.equals(required(expectedTargetType, "expectedTargetType"))
                || !targetId.equals(required(expectedTargetId, "expectedTargetId"))) {
            throw new IllegalArgumentException("BAT risk command target does not match the invoked operation");
        }
    }

    private static String field(String value) {
        return value.length() + ":" + value;
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
