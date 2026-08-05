package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Agent에 전달되는 durable Runtime 변경 단위입니다. */
public record CpfRuntimeDelivery(
        String deliveryId,
        String changeId,
        String changeType,
        String instanceId,
        long desiredVersion,
        long fencingToken,
        String requestHash,
        String payloadHash,
        int payloadSchemaVersion,
        CpfRuntimePayload payload,
        int attempt,
        Instant expiresAt) {

    public CpfRuntimeDelivery {
        deliveryId = requireText(deliveryId, "deliveryId");
        changeId = requireText(changeId, "changeId");
        changeType = requireText(changeType, "changeType");
        instanceId = requireText(instanceId, "instanceId");
        requestHash = requireText(requestHash, "requestHash");
        payloadHash = requireText(payloadHash, "payloadHash");
        if (desiredVersion < 0L) {
            throw new IllegalArgumentException("desiredVersion은 0 이상이어야 합니다.");
        }
        if (fencingToken < 0L) {
            throw new IllegalArgumentException("fencingToken은 0 이상이어야 합니다.");
        }
        if (payloadSchemaVersion < 1) {
            throw new IllegalArgumentException("payloadSchemaVersion은 1 이상이어야 합니다.");
        }
        if (attempt < 0) {
            throw new IllegalArgumentException("attempt는 0 이상이어야 합니다.");
        }
        payload = payload == null ? CpfRuntimePayload.empty() : payload;
    }

    /** 기존 delivery 생성 코드와 동일한 schema version 기본값을 제공하는 Typed 생성자입니다. */
    public CpfRuntimeDelivery(String deliveryId, String changeId, String changeType, String instanceId,
                              long desiredVersion, long fencingToken, String requestHash, String payloadHash,
                              CpfRuntimePayload payload, int attempt, Instant expiresAt) {
        this(deliveryId, changeId, changeType, instanceId, desiredVersion, fencingToken, requestHash,
                payloadHash, 1, payload, attempt, expiresAt);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
            throw new IllegalArgumentException(field + "는 비어 있을 수 없습니다.");
        }
        return value.trim();
    }
}
