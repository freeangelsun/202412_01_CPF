package com.cpf.core.api.transaction;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Retry/Recovery에서도 유지되는 transactionId와 별도 attempt 식별자를 전달합니다.
 */
public record CpfTransactionContext(
        String transactionId,
        String attemptId,
        String segmentId,
        Instant startedAt,
        Map<String, String> attributes) {

    public CpfTransactionContext {
        transactionId = requireText(transactionId, "transactionId");
        attemptId = requireText(attemptId, "attemptId");
        segmentId = requireText(segmentId, "segmentId");
        Objects.requireNonNull(startedAt, "startedAt");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
