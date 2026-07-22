package com.cpf.core.common.idempotency;

/**
 * CPF 멱등 처리의 표준 상태입니다.
 */
public enum CpfIdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED,
    UNKNOWN,
    EXPIRED;

    public static CpfIdempotencyStatus from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return UNKNOWN;
        }
    }
}
