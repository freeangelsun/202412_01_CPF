package com.cpf.reference.transaction;

/**
 * 상태 전이와 transactionId 연결을 표현하는 샘플입니다.
 */
public class ReferenceTransactionEducationSample {

    public TransactionStep changeStatus(String transactionId, String beforeStatus, String afterStatus) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId는 필수입니다.");
        }
        return new TransactionStep(transactionId, beforeStatus, afterStatus, "COMMIT");
    }

    public record TransactionStep(String transactionId, String beforeStatus, String afterStatus, String action) {
    }
}
