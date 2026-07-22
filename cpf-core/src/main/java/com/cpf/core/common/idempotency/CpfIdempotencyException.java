package com.cpf.core.common.idempotency;

/**
 * 멱등 key 충돌과 처리 중 중복을 업무 오류와 구분해 전달합니다.
 */
public class CpfIdempotencyException extends RuntimeException {
    private final String code;

    public CpfIdempotencyException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() {
        return code;
    }
}
