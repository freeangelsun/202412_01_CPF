package com.cpf.core.common.exception;

/** 외부 서비스 호출 실패를 CPF 표준 오류 응답으로 변환하는 예외입니다. */
public class CpfExternalServiceException extends CpfException {
    public CpfExternalServiceException(String detail, Throwable cause) {
        super(CpfErrorCode.EXTERNAL_SERVICE_ERROR, null, null, detail, cause);
    }
}

