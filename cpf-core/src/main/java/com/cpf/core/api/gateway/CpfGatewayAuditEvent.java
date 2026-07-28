package com.cpf.core.api.gateway;

import java.time.Instant;
import java.util.Map;

/** Gateway 위험 거래의 사전/결과 Audit 이벤트입니다. 원문 credential/body는 포함하지 않습니다. */
public record CpfGatewayAuditEvent(
        String transactionId,
        String standardExecutionId,
        String principalId,
        String reason,
        String phase,
        String outcome,
        String targetInstanceId,
        Integer httpStatus,
        Instant occurredAt,
        Map<String, String> attributes) {
    public CpfGatewayAuditEvent {
        occurredAt = occurredAt == null ? Instant.now() : occurredAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
