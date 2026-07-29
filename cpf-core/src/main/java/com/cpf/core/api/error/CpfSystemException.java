package com.cpf.core.api.error;

/** 복구할 수 없는 내부 처리 실패를 표준 오류 경로로 전달하는 공개 예외입니다. */
public class CpfSystemException extends com.cpf.core.common.exception.CpfSystemException {

    public CpfSystemException(String detail, Throwable cause) {
        super(detail, cause);
    }
}
