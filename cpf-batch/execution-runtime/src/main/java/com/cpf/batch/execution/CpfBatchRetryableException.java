package com.cpf.batch.execution;
public final class CpfBatchRetryableException extends CpfBatchExecutionException {
    public CpfBatchRetryableException(String code, String message) { super(code, message); }
}
