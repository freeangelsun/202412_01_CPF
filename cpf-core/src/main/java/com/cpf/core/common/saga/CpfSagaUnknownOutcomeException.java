package com.cpf.core.common.saga;
/** External side effect result cannot be determined; automatic compensation must not guess. */
public class CpfSagaUnknownOutcomeException extends RuntimeException {
    public CpfSagaUnknownOutcomeException(String message){super(message);}
    public CpfSagaUnknownOutcomeException(String message,Throwable cause){super(message,cause);}
}
