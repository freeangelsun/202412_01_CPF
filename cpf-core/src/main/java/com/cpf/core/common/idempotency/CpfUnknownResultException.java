package com.cpf.core.common.idempotency;

/**
 * 호출 결과를 성공 또는 실패로 확정할 수 없어 reconciliation이 필요한 경우 사용합니다.
 */
public class CpfUnknownResultException extends RuntimeException {
    public CpfUnknownResultException(String message) {
        super(message);
    }

    public CpfUnknownResultException(String message, Throwable cause) {
        super(message, cause);
    }
}
