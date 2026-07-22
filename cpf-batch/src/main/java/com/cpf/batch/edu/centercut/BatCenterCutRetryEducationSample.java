package cpf.bat.edu.centercut;

/**
 * 실패 item 재처리 가능 여부를 판단하는 샘플입니다.
 */
public class BatCenterCutRetryEducationSample {

    public boolean retryable(String failureCode) {
        return "TIMEOUT".equals(failureCode) || "TEMPORARY".equals(failureCode);
    }
}
