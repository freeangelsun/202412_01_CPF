package com.cpf.messaging.api;

import java.util.Arrays;
import java.util.Map;

/**
 * Provider-neutral event request stored through the CPF messaging reliability path.
 *
 * <p>Core에서 Messaging Owner로 이동하되 기존 공개 계약의 field order와 default semantics를
 * 그대로 보존합니다. transaction/idempotency/context 강제는 Provider/Runtime boundary가 담당합니다.</p>
 */
public record CpfBrokerPublishRequest(
        String messageId,
        String topic,
        String key,
        byte[] payload,
        String contentType,
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
        headers = validatedMap(headers, "headers");
        attributes = validatedMap(attributes, "attributes");
    }

    @Override
    public byte[] payload() {
        return Arrays.copyOf(payload, payload.length);
    }

    private static Map<String, String> validatedMap(Map<String, String> values, String label) {
        if (values == null || values.isEmpty()) return Map.of();
        java.util.LinkedHashMap<String, String> copy = new java.util.LinkedHashMap<>();
        for (var entry : values.entrySet()) {
            String name = require(entry.getKey(), label + " key");
            String value = require(entry.getValue(), label + "[" + name + "]");
            if (copy.put(name, value) != null) throw new IllegalArgumentException(label + " contains duplicate key: " + name);
        }
        return Map.copyOf(copy);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
