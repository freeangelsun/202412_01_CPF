package com.cpf.core.common.exception;

/** 복구할 수 없는 내부 처리 실패를 감싸는 CPF 표준 시스템 예외입니다. */
public class CpfSystemException extends CpfException {
    public CpfSystemException(String detail, Throwable cause) {
        super(CpfErrorCode.INTERNAL_SERVER_ERROR, null, null, detail, cause);
    }
}

