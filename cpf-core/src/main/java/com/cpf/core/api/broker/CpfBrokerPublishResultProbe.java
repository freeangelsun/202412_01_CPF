package com.cpf.core.api.broker;

/** Provider-neutral query for an ambiguous publish result. */
public record CpfBrokerPublishResultProbe(String idempotencyKey, String transactionId, String destination) {
    public CpfBrokerPublishResultProbe {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey is required");
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId is required");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("destination is required");
        idempotencyKey = idempotencyKey.trim(); transactionId = transactionId.trim(); destination = destination.trim();
    }
}
