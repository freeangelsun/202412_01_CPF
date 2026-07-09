package cpf.bat.edu.transaction;

import java.util.List;

/**
 * 배치 단위 commit/rollback 경계를 설명하는 샘플입니다.
 */
public class BatTransactionEducationSample {

    public TransactionSummary summarize(List<Boolean> itemResults) {
        long success = itemResults.stream().filter(Boolean::booleanValue).count();
        long failure = itemResults.size() - success;
        return new TransactionSummary(itemResults.size(), success, failure, failure == 0 ? "COMMIT" : "ROLLBACK");
    }

    public record TransactionSummary(int requested, long success, long failure, String action) {
    }
}
