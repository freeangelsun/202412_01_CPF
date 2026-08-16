package com.cpf.integration.ai.api;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

/** Request after application policy has selected/masked the payload. */
/** CpfAiRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfAiRequest(
        String transactionId,
        String model,
        String maskedPayload,
        CpfAiRisk risk,
        Duration timeout,
        boolean humanApproved,
        Map<String, String> attributes) {
    public CpfAiRequest {
        if (transactionId == null || transactionId.isBlank()) throw new IllegalArgumentException("transactionId");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model");
        Objects.requireNonNull(risk, "risk");
        if (timeout == null || timeout.isZero() || timeout.isNegative()) throw new IllegalArgumentException("timeout");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
