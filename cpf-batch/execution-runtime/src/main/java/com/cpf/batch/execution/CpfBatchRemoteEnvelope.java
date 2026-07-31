package com.cpf.batch.execution;

import java.time.Instant;
import java.util.Map;

/** Kafka로 전달하는 Spring Batch Remote 요청/응답 Envelope입니다. */
public record CpfBatchRemoteEnvelope(
        String messageId,
        String payloadType,
        String payloadJson,
        Map<String, Object> headers,
        Instant createdAt) {
    public CpfBatchRemoteEnvelope {
        if (messageId == null || messageId.isBlank()) throw new IllegalArgumentException("messageId is required");
        if (payloadType == null || payloadType.isBlank()) throw new IllegalArgumentException("payloadType is required");
        if (payloadJson == null) throw new IllegalArgumentException("payloadJson is required");
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
