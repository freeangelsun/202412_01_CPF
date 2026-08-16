package com.cpf.integration.resilience.spi;

import java.time.Instant;
import java.util.Map;

/** Fail-closed structured audit sink; payload values must already be sanitized. */
/** CpfResilienceAuditSink 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfResilienceAuditSink {
    void record(String eventType, String operationId, String actorId, String reason,
                Map<String, String> sanitizedAttributes, Instant occurredAt);
}
