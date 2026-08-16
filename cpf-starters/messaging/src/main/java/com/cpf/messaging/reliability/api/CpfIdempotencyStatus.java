package com.cpf.messaging.reliability.api;

/**
 * CPF 멱등 처리의 표준 상태입니다.
 */
public enum CpfIdempotencyStatus {
    PROCESSING,
    SUCCESS,
    FAILED,
    UNKNOWN,
    EXPIRED;

    /** from 작업을 CPF 표준 계약에 따라 수행한다. */
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
