package com.cpf.core.api.transaction;

/**
 * 업무 코드가 특정 Spring/JTA Provider에 직접 결합되지 않도록 하는 CPF 거래 편의 API입니다.
 */
public interface CpfTransactionOperations {
    <T> T execute(CpfTransactionDefinition definition, CpfTransactionWork<T> work);
}
