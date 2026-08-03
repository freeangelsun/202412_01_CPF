package com.cpf.core.spi.resilience;

import java.time.Instant;
import java.util.Map;

/** Fail-closed structured audit sink; payload values must already be sanitized. */
public interface CpfResilienceAuditSink {
    void record(String eventType, String operationId, String actorId, String reason,
                Map<String, String> sanitizedAttributes, Instant occurredAt);
}
