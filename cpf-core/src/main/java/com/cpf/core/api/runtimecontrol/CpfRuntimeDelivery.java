package com.cpf.core.api.runtimecontrol;

import java.time.Instant;
import java.util.Map;

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
        Map<String, Object> payload,
        int attempt,
        Instant expiresAt) {

    public CpfRuntimeDelivery {
        payload = payload == null ? Map.of() : Map.copyOf(payload);
        payloadSchemaVersion = payloadSchemaVersion <= 0 ? 1 : payloadSchemaVersion;
    }

    /** 기존 delivery 생성 코드 호환입니다. */
    public CpfRuntimeDelivery(String deliveryId, String changeId, String changeType, String instanceId,
                              long desiredVersion, long fencingToken, String requestHash, String payloadHash,
                              Map<String, Object> payload, int attempt, Instant expiresAt) {
        this(deliveryId, changeId, changeType, instanceId, desiredVersion, fencingToken, requestHash,
                payloadHash, 1, payload, attempt, expiresAt);
    }
}
