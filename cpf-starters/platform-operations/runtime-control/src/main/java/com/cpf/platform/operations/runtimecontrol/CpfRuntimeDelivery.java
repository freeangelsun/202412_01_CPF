package com.cpf.platform.operations.runtimecontrol;

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
        payload = payload == null ? CpfRuntimePayload.empty() : payload;
        payloadSchemaVersion = payloadSchemaVersion <= 0 ? 1 : payloadSchemaVersion;
    }

    /** 기존 delivery 생성 코드와 동일한 schema version 기본값을 제공하는 Typed 생성자입니다. */
    public CpfRuntimeDelivery(String deliveryId, String changeId, String changeType, String instanceId,
                              long desiredVersion, long fencingToken, String requestHash, String payloadHash,
                              CpfRuntimePayload payload, int attempt, Instant expiresAt) {
        this(deliveryId, changeId, changeType, instanceId, desiredVersion, fencingToken, requestHash,
                payloadHash, 1, payload, attempt, expiresAt);
    }
}
