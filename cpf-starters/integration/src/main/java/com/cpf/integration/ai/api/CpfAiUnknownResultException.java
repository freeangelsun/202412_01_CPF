package com.cpf.integration.ai.api;

/** Side-effecting provider timeout/failure where final provider result is unknown. */
/** CpfAiUnknownResultException 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public final class CpfAiUnknownResultException extends RuntimeException {
    private final String transactionId;
    public CpfAiUnknownResultException(String transactionId, Throwable cause) {
        super("AI result is UNKNOWN; transactionId=" + transactionId, cause);
        this.transactionId = transactionId;
    }
    public String transactionId() { return transactionId; }
}
