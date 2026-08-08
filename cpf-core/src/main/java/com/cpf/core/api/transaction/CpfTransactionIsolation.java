package com.cpf.core.api.transaction;

/** CPF 거래 격리 수준입니다. */
public enum CpfTransactionIsolation {
    DEFAULT,
    READ_UNCOMMITTED,
    READ_COMMITTED,
    REPEATABLE_READ,
    SERIALIZABLE
}
