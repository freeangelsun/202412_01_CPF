package com.cpf.messaging.api;

import java.time.Instant;
import java.util.Map;

/** Provider-neutral CPF broker message. */
/** CpfBrokerBridgeMessage 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfBrokerBridgeMessage(String transport, String destination, String key, Object payload, Map<String,String> headers, Instant createdAt) {
    public CpfBrokerBridgeMessage {
        if (transport == null || transport.isBlank()) throw new IllegalArgumentException("transport is required");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("destination is required");
        if (key == null || key.isBlank()) throw new IllegalArgumentException("key is required");
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        if (createdAt == null) throw new IllegalArgumentException("createdAt is required");
    }
}
