package com.cpf.messaging.reliability.api;

/**
 * 멱등 key 충돌과 처리 중 중복을 업무 오류와 구분해 전달합니다.
 */
public class CpfIdempotencyException extends RuntimeException {
    private final String code;

    public CpfIdempotencyException(String code, String message) {
        super(message);
        this.code = code;
    }

    /** code 작업을 CPF 표준 계약에 따라 수행한다. */
    public String code() {
        return code;
    }
}
