package com.cpf.integration.ai.api;

/** Provider-neutral AI operation contract. */
@FunctionalInterface
/** CpfAiOperations 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfAiOperations {
    CpfAiResponse execute(CpfAiRequest request);
}
