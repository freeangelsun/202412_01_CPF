package com.cpf.education.data.transaction;
/**
 * 상태 전이와 transactionId 연결을 표현하는 샘플입니다.
 */
public class EducationTransactionEducationSample {

    public TransactionStep changeStatus(String transactionId, String beforeStatus, String afterStatus) {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId는 필수입니다.");
        }
        return new TransactionStep(transactionId, beforeStatus, afterStatus, "COMMIT");
    }

    /** TransactionStep 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    public record TransactionStep(String transactionId, String beforeStatus, String afterStatus, String action) {
    }
}
