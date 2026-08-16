package com.cpf.reliability.api;

/** 멱등 Runtime의 충돌/진행중/UNKNOWN/구성 오류를 명시적으로 노출하는 기술 예외입니다. */
public final class CpfIdempotencyException extends RuntimeException {
    private final String code;
    public CpfIdempotencyException(String code, String message) { super(message); this.code = code; }
    public CpfIdempotencyException(String code, String message, Throwable cause) { super(message, cause); this.code = code; }
    public String code() { return code; }
}
