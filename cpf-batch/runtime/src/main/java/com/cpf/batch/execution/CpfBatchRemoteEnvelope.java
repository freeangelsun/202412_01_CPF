package com.cpf.batch.execution;

import java.time.Instant;
import java.util.Map;

/** Versioned Kafka Remote Batch 요청/응답 Envelope입니다. */
public record CpfBatchRemoteEnvelope(
        int schemaVersion,
        String messageId,
        String correlationId,
        String producerId,
        String environment,
        String tenantId,
        int attempt,
        String replyTopic,
        String payloadType,
        String payloadJson,
        String payloadSha256,
        Map<String, Object> headers,
        Instant createdAt,
        Instant expiresAt) {
    public static final int CURRENT_SCHEMA_VERSION = 2;
    public CpfBatchRemoteEnvelope {
        if (schemaVersion != CURRENT_SCHEMA_VERSION) throw new IllegalArgumentException("unsupported schemaVersion");
        required(messageId, "messageId"); required(correlationId, "correlationId"); required(producerId, "producerId");
        required(environment, "environment"); required(tenantId, "tenantId"); required(payloadType, "payloadType");
        required(payloadSha256, "payloadSha256");
        if (attempt < 1) throw new IllegalArgumentException("attempt must be positive");
        if (payloadJson == null) throw new IllegalArgumentException("payloadJson is required");
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        if (createdAt == null || expiresAt == null || !expiresAt.isAfter(createdAt)) throw new IllegalArgumentException("valid envelope lifetime is required");
    }
    private static void required(String value,String name){if(value==null||value.isBlank())throw new IllegalArgumentException(name+" is required");}
}
