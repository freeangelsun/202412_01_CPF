package com.cpf.core.api.transaction;

/**
 * CPF 34자리 transactionId 발급/승계 공개 계약입니다.
 *
 * <p>외부에서 유효한 transactionId가 전달되면 승계하고, 독립 실행은 구현체가 신규 발급합니다.</p>
 */
public interface CpfTransactionIdGenerator {
    int TRANSACTION_ID_LENGTH = 34;

    String generate();
    String generateOrUse(String incomingTransactionId);
    boolean isValid(String transactionId);
    String getModuleId();
    String getInstanceToken();
}
