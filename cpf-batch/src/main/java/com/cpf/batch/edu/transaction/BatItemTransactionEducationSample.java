package cpf.bat.edu.transaction;

/**
 * item 단위 트랜잭션 처리 결과 샘플입니다.
 */
public class BatItemTransactionEducationSample {

    public ItemResult process(long itemNo, boolean valid) {
        return new ItemResult(itemNo, valid ? "COMMITTED" : "SKIPPED", valid ? null : "VALIDATION_FAILED");
    }

    public record ItemResult(long itemNo, String status, String reason) {
    }
}
