package com.cpf.integration.ai.api;

import java.util.Map;

/** CpfAiResponse 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfAiResponse(
        String provider,
        String model,
        String maskedOutput,
        CpfAiUsage usage,
        Map<String, String> attributes) {
    public CpfAiResponse {
        if (provider == null || provider.isBlank()) throw new IllegalArgumentException("provider");
        if (model == null || model.isBlank()) throw new IllegalArgumentException("model");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
