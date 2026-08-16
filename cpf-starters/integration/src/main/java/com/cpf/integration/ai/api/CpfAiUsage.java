package com.cpf.integration.ai.api;

/** CpfAiUsage 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfAiUsage(long inputUnits, long outputUnits, long totalUnits) {
    public CpfAiUsage {
        if (inputUnits < 0 || outputUnits < 0 || totalUnits < 0) throw new IllegalArgumentException("usage must be >= 0");
        if (totalUnits < inputUnits || totalUnits < outputUnits) throw new IllegalArgumentException("invalid total usage");
    }
}
