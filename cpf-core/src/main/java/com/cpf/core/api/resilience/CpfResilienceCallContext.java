package com.cpf.core.api.resilience;

import java.time.Instant;
import java.util.Map;

/** Correlation and idempotency context for a resilient outbound call. */
public record CpfResilienceCallContext(
        String operationId, String transactionId, String idempotencyKey,
        Instant requestedAt, Map<String, String> attributes) {
    public CpfResilienceCallContext {
        operationId = required(operationId, "operationId");
        transactionId = required(transactionId, "transactionId");
        idempotencyKey = idempotencyKey == null || idempotencyKey.isBlank() ? null : idempotencyKey.trim();
        requestedAt = requestedAt == null ? Instant.now() : requestedAt;
        attributes = Map.copyOf(attributes == null ? Map.of() : attributes);
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
