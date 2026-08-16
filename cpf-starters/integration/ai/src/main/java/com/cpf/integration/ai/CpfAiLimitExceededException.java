package com.cpf.integration.ai;

/** Fail-closed AI quota/resource rejection. Contains no prompt or credential material. */
public final class CpfAiLimitExceededException extends RuntimeException {
    private final String code;
    public CpfAiLimitExceededException(String code, String message) {
        super(message);
        if (code == null || code.isBlank()) throw new IllegalArgumentException("code");
        this.code = code;
    }
    public String code() { return code; }
}
