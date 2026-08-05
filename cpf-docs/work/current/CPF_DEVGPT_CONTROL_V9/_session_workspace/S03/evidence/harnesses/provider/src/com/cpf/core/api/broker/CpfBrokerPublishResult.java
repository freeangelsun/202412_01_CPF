package com.cpf.core.api.broker;

import java.time.Instant;
import java.util.Locale;

/** Provider-neutral enqueue/publish result with explicit success, failure, and unknown states. */
public record CpfBrokerPublishResult(
        String status,
        String messageId,
        String brokerName,
        String partitionKey,
        Instant processedAt,
        String detail) {

    public CpfBrokerPublishResult {
        status = status == null || status.isBlank()
                ? "UNKNOWN"
                : status.trim().toUpperCase(Locale.ROOT);
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }
}
