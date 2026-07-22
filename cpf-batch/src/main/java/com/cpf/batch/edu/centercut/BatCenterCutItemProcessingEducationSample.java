package cpf.bat.edu.centercut;

/**
 * center-cut item 단위 처리 상태 샘플입니다.
 */
public class BatCenterCutItemProcessingEducationSample {

    public ItemProcessResult process(String itemId, boolean valid) {
        return new ItemProcessResult(itemId, valid ? "SUCCESS" : "FAILED");
    }

    public record ItemProcessResult(String itemId, String status) {
    }
}
