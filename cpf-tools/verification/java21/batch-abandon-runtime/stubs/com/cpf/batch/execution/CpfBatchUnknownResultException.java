package com.cpf.batch.execution;
public final class CpfBatchUnknownResultException extends RuntimeException {
 private final String code; public CpfBatchUnknownResultException(String code,String message){super(message);this.code=code;} public String code(){return code;}
}
