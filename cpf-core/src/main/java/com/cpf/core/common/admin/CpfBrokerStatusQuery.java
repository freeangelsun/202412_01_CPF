package com.cpf.core.common.admin;

import java.time.Instant;

/**
 * ADM broker 관제 후보 조회 조건입니다.
 */
public record CpfBrokerStatusQuery(
        String brokerName,
        String brokerType,
        String status,
        Instant from,
        Instant to) {
}
