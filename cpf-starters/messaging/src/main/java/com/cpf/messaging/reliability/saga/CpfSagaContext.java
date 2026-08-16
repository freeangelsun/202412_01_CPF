package com.cpf.messaging.reliability.saga;

import java.util.Map;

/** 하나의 durable Saga 실행에 필요한 불변 업무 식별자와 bounded attribute입니다. */
public record CpfSagaContext(
        String sagaId,
        String sagaType,
        String businessKey,
        String transactionId,
        Map<String, Object> attributes) {
    public CpfSagaContext {
        sagaId = required(sagaId, "sagaId", 180);
        sagaType = required(sagaType, "sagaType", 80);
        businessKey = required(businessKey, "businessKey", 256);
        transactionId = required(transactionId, "transactionId", 180);
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
        if (attributes.size() > 64) throw new IllegalArgumentException("Saga attributes는 64개 이하여야 합니다.");
    }

    private static String required(String value, String name, int maxLength) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " 필수");
        String normalized = value.trim();
        if (normalized.length() > maxLength) throw new IllegalArgumentException(name + " 길이 초과");
        return normalized;
    }
}
