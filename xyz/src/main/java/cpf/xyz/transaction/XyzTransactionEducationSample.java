package cpf.xyz.transaction;

/**
 * 상태 전이와 transactionGlobalId 연결을 표현하는 샘플입니다.
 */
public class XyzTransactionEducationSample {

    public TransactionStep changeStatus(String transactionGlobalId, String beforeStatus, String afterStatus) {
        if (transactionGlobalId == null || transactionGlobalId.isBlank()) {
            throw new IllegalArgumentException("transactionGlobalId는 필수입니다.");
        }
        return new TransactionStep(transactionGlobalId, beforeStatus, afterStatus, "COMMIT");
    }

    public record TransactionStep(String transactionGlobalId, String beforeStatus, String afterStatus, String action) {
    }
}
