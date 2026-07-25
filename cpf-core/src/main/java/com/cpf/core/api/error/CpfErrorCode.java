package com.cpf.core.api.error;

import org.springframework.http.HttpStatus;

/**
 * 업무 Domain에서 사용할 수 있는 CPF 공개 표준 오류 코드입니다.
 *
 * <p>Runtime 내부 오류 정의를 외부 모듈에 노출하지 않으면서 동일한 상태/메시지 계약을 유지합니다.</p>
 */
public enum CpfErrorCode {
    INVALID_PARAMETER(com.cpf.core.common.exception.CpfErrorCode.INVALID_PARAMETER),
    NOT_FOUND(com.cpf.core.common.exception.CpfErrorCode.NOT_FOUND),
    DUPLICATE(com.cpf.core.common.exception.CpfErrorCode.DUPLICATE),
    VALIDATION_FAILED(com.cpf.core.common.exception.CpfErrorCode.VALIDATION_FAILED),
    UNAUTHORIZED(com.cpf.core.common.exception.CpfErrorCode.UNAUTHORIZED),
    FORBIDDEN(com.cpf.core.common.exception.CpfErrorCode.FORBIDDEN),
    BUSINESS_RULE_VIOLATION(com.cpf.core.common.exception.CpfErrorCode.BUSINESS_RULE_VIOLATION),
    EXTERNAL_SERVICE_ERROR(com.cpf.core.common.exception.CpfErrorCode.EXTERNAL_SERVICE_ERROR),
    INTERNAL_SERVER_ERROR(com.cpf.core.common.exception.CpfErrorCode.INTERNAL_SERVER_ERROR),
    DATABASE_ERROR(com.cpf.core.common.exception.CpfErrorCode.DATABASE_ERROR);

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
}
