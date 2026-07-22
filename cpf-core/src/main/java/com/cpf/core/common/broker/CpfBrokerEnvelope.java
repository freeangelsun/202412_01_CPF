package com.cpf.core.common.broker;

import java.time.Instant;
import java.util.Map;

/**
 * broker 메시지의 거래 추적 envelope입니다.
 *
 * <p>transactionGlobalId와 segmentId를 broker 전달 단위에 항상 붙여 온라인 거래,
 * 배치, 외부연계가 같은 추적 기준으로 연결되게 합니다.</p>
 */
public record CpfBrokerEnvelope(
        String transactionGlobalId,
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
