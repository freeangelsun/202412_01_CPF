package com.cpf.core.api.error;

import org.springframework.http.HttpStatus;

/**
 * 업무 Domain과 CPF 외부 Module에서 사용할 수 있는 공개 표준 오류 코드입니다.
 *
 * <p>외부 Consumer는 {@code com.cpf.core.common.*} 내부 구현 대신 이 계약을 사용합니다.
 * HTTP 상태, 외부 메시지와 메시지 코드는 CPF 공통 예외 처리기에서 동일하게 해석됩니다.</p>
 */
public enum CpfErrorCode implements CpfErrorDefinition {
    INVALID_PARAMETER(com.cpf.core.common.exception.CpfErrorCode.INVALID_PARAMETER),
    NOT_FOUND(com.cpf.core.common.exception.CpfErrorCode.NOT_FOUND),
    DUPLICATE(com.cpf.core.common.exception.CpfErrorCode.DUPLICATE),
    CONFLICT(com.cpf.core.common.exception.CpfErrorCode.CONFLICT),
    VALIDATION_FAILED(com.cpf.core.common.exception.CpfErrorCode.VALIDATION_FAILED),
    UNAUTHORIZED(com.cpf.core.common.exception.CpfErrorCode.UNAUTHORIZED),
    FORBIDDEN(com.cpf.core.common.exception.CpfErrorCode.FORBIDDEN),
    BUSINESS_RULE_VIOLATION(com.cpf.core.common.exception.CpfErrorCode.BUSINESS_RULE_VIOLATION),
    EXTERNAL_SERVICE_ERROR(com.cpf.core.common.exception.CpfErrorCode.EXTERNAL_SERVICE_ERROR),
    INTERNAL_SERVER_ERROR(com.cpf.core.common.exception.CpfErrorCode.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(com.cpf.core.common.exception.CpfErrorCode.DATABASE_ERROR),
    INFRASTRUCTURE_UNAVAILABLE(com.cpf.core.common.exception.CpfErrorCode.INFRASTRUCTURE_UNAVAILABLE);

    private final com.cpf.core.common.exception.CpfErrorCode delegate;

    CpfErrorCode(com.cpf.core.common.exception.CpfErrorCode delegate) {
        this.delegate = delegate;
    }

    com.cpf.core.common.exception.CpfErrorDefinition internalDefinition() {
        return delegate;
    }

    public String statusCode() { return delegate.getStatusCode(); }
    public String messageCode() { return delegate.getMessageCode(); }
    public HttpStatus httpStatus() { return delegate.getHttpStatus(); }
    public String defaultExternalMessage() { return delegate.getDefaultExternalMessage(); }
    public String defaultInternalMessage() { return delegate.getDefaultInternalMessage(); }
    @Override public String getStatusCode() { return statusCode(); }
    @Override public String getMessageCode() { return messageCode(); }
    @Override public HttpStatus getHttpStatus() { return httpStatus(); }
    @Override public String getDefaultExternalMessage() { return defaultExternalMessage(); }
    @Override public String getDefaultInternalMessage() { return defaultInternalMessage(); }
}
