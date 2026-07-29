package com.cpf.core.api.broker;

import java.time.Instant;
import java.util.Map;

/**
 * 프로젝트 공통 메시징 facade가 broker 구현과 무관하게 사용하는 공개 메시지 봉투입니다.
 */
public record CpfBrokerBridgeMessage(
        String broker,
        String destination,
        String key,
        Object payload,
        Map<String, String> headers,
        Instant createdAt) {

    public CpfBrokerBridgeMessage {
        headers = headers == null ? Map.of() : Map.copyOf(headers);
        createdAt = createdAt == null ? Instant.now() : createdAt;
    }
}
