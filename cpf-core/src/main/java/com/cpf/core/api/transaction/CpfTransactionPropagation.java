package com.cpf.core.api.transaction;

/** CPF 거래 전파 정책입니다. */
public enum CpfTransactionPropagation {
    REQUIRED,
    REQUIRES_NEW,
    SUPPORTS,
    MANDATORY,
    NEVER,
    NOT_SUPPORTED
}
