package com.cpf.web.context;

import com.cpf.core.api.error.CpfFrameworkErrorCode;

/**
 * CPF 표준 Header 경계 검증 실패를 나타냅니다.
 * 원문 인증/Secret 값은 보관하지 않고 Header 이름과 표준 오류 의미만 제공합니다.
 */
public final class CpfHeaderValidationException extends IllegalArgumentException {
    private final CpfFrameworkErrorCode errorCode;
    private final String headerName;

    public CpfHeaderValidationException(CpfFrameworkErrorCode errorCode, String headerName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.headerName = headerName;
    }

    public CpfFrameworkErrorCode errorCode() { return errorCode; }
    public String headerName() { return headerName; }
}
