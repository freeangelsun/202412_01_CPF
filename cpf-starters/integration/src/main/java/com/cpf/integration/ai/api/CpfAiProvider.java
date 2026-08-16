package com.cpf.integration.ai.api;

/** Swappable provider boundary. No vendor type may escape through this contract. */
/** CpfAiProvider 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfAiProvider {
    String providerId();
    boolean supports(String model);
    boolean safeToFallbackAfterTimeout();
    CpfAiResponse execute(CpfAiRequest request) throws Exception;
}
