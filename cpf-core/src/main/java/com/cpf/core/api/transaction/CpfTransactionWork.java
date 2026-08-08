package com.cpf.core.api.transaction;

/** 거래 경계 안에서 실행되는 업무 작업입니다. */
@FunctionalInterface
/** CPF Transaction 경계 안에서 실행할 업무 작업을 나타냅니다. */
public interface CpfTransactionWork<T> {
    T execute(CpfTransactionContext context) throws Exception;
}
