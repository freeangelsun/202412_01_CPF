package com.cpf.starter.integration.ai;
/** Provider timeout/실패로 최종 결과를 확정하지 못한 경우. prompt 원문은 보존하지 않습니다. */
public final class CpfAiUnknownResultException extends RuntimeException { private final String transactionId; public CpfAiUnknownResultException(String tx,Throwable cause){super("AI result is UNKNOWN for transactionId="+tx,cause);transactionId=tx;} public String transactionId(){return transactionId;} }
