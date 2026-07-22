package com.cpf.core.common.exception;

/** 요청한 업무 자원을 찾지 못한 경우 사용하는 CPF 표준 예외입니다. */
public class CpfNotFoundException extends CpfException {
    public CpfNotFoundException(String detail) {
        super(CpfErrorCode.NOT_FOUND, detail);
    }
}

