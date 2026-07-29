package com.cpf.core.api.error;

/** 외부 서비스·보안 Provider 호출 실패를 표준 오류 경로로 전달하는 공개 예외입니다. */
public class CpfExternalServiceException
        extends com.cpf.core.common.exception.CpfExternalServiceException {

    public CpfExternalServiceException(String detail, Throwable cause) {
        super(detail, cause);
    }
}
