package com.cpf.core.api.fixedlength;

/**
 * CPF 고정길이 필드가 표현하는 표준 논리 자료형입니다.
 */
public enum CpfFixedLengthFieldType {
    STRING,
    NUMBER,
    DECIMAL,
    AMOUNT,
    DATE,
    TIME,
    BOOLEAN,
    CUSTOM
}
