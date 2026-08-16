package com.cpf.integration.ai.api;

/** Security/masking/audit policy boundary for AI calls. */
/** CpfAiPolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public interface CpfAiPolicy {
    CpfAiRequest authorizeAndMask(CpfAiRequest request);
    void audit(CpfAiRequest request, CpfAiResponse response, Throwable failure);
}
