package com.cpf.external.execution.port;

/** 요청 전송 후 처리 결과를 확정할 수 없어 재조회 또는 수동 복구가 필요한 경우입니다. */
public class ExternalUnknownResultException extends RuntimeException {

    private final String failureCode;

    public ExternalUnknownResultException(String failureCode, String message, Throwable cause) {
        super(message, cause);
        this.failureCode = failureCode;
    }

    public String failureCode() {
        return failureCode;
    }
}
