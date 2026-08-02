package com.cpf.core.api.broker;

import java.util.Arrays;
import java.util.Map;

/** Provider-neutral event request stored through the CPF transactional reliability path. */
public record CpfBrokerPublishRequest(
        String messageId,
        String topic,
        String key,
        byte[] payload,
        String contentType,
        String transactionId,
        String segmentId,
        String producerModule,
        String consumerModule,
        String idempotencyKey,
        Map<String, String> headers,
        Map<String, String> attributes) {

    public CpfBrokerPublishRequest {
        messageId = require(messageId, "messageId");
        topic = require(topic, "topic");
        key = key == null || key.isBlank() ? messageId : key.trim();
        payload = payload == null ? new byte[0] : Arrays.copyOf(payload, payload.length);
        contentType = contentType == null || contentType.isBlank()
                ? "application/octet-stream"
                : contentType.trim();
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
