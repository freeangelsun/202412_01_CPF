package com.cpf.core.common.broker;

import java.time.Instant;

/**
 * broker 이력 조회 조건입니다.
 */
public record CpfBrokerHistoryQuery(
        String brokerName,
        String topic,
        String messageId,
        String transactionGlobalId,
        String status,
        Instant from,
        Instant to,
        int limit) {

    public CpfBrokerHistoryQuery {
        limit = limit <= 0 ? 100 : Math.min(limit, 1000);
    }
}
