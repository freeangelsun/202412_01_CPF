package com.cpf.batch.execution;

public class CpfBatchExecutionException extends RuntimeException {
    private final String code;
    public CpfBatchExecutionException(String code, String message) {
        super(message == null || message.isBlank() ? code : message);
        this.code = code == null || code.isBlank() ? "BATCH_EXECUTION_FAILED" : code;
    }
    public String code() { return code; }
}
