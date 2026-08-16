package com.cpf.messaging.spi.broker;

import java.time.Instant;
import java.util.Map;

/**
 * Broker 메시지의 CPF 거래 추적 envelope입니다.
 *
 * <p>{@code transactionId}는 최초 진입부터 local/remote/async/batch 후속 처리까지
 * 동일 업무 흐름 전체가 승계하는 유일한 전역 거래 ID입니다. 세부 호출 계층은 segmentId로 구분합니다.</p>
 */
public record CpfBrokerEnvelope(
        String transactionId,
        String segmentId,
        String producerModule,
        String consumerModule,
        String idempotencyKey,
        Instant occurredAt,
        CpfBrokerMessage message,
        Map<String, String> attributes) {

    public CpfBrokerEnvelope {
        if (message == null) {
            throw new IllegalArgumentException("message는 필수입니다.");
        }
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

}
