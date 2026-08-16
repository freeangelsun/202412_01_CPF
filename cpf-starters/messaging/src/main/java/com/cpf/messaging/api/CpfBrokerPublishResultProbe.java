package com.cpf.messaging.api;

/** Provider-neutral query for an ambiguous publish result. */
/** CpfBrokerPublishResultProbe 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfBrokerPublishResultProbe(String idempotencyKey, String transactionId, String destination) {
    public CpfBrokerPublishResultProbe {
        if (idempotencyKey == null || idempotencyKey.isBlank()) throw new IllegalArgumentException("idempotencyKey is required");
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId is required");
        if (destination == null || destination.isBlank()) throw new IllegalArgumentException("destination is required");
        idempotencyKey = idempotencyKey.trim(); transactionId = transactionId.trim(); destination = destination.trim();
    }
}
