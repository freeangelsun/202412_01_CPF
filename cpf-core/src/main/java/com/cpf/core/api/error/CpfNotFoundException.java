package com.cpf.core.api.error;

/** 요청한 업무 자원을 찾지 못한 경우 사용하는 CPF 공개 표준 예외입니다. */
public class CpfNotFoundException extends com.cpf.core.common.exception.CpfNotFoundException {
    public CpfNotFoundException(String detail) {
        super(detail);
    }
}
