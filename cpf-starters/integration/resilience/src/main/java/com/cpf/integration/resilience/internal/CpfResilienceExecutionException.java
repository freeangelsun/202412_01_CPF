package com.cpf.integration.resilience.internal;

/** Signals a framework/audit defect separately from a remote-call outcome. */
public final class CpfResilienceExecutionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CpfResilienceExecutionException(String message, Throwable cause) { super(message, cause); }
}
