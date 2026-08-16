package com.cpf.integration.fixedlength.api;

/** CUSTOM 고정길이 필드의 문자열↔업무값 변환 확장점입니다. */
public interface CpfFixedLengthValueConverter {
    Object parse(String value);
    String write(Object value);
}
