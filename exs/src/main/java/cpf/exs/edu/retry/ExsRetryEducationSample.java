package cpf.exs.edu.retry;

/**
 * 대외기관 일시 장애 retry/failover 후보 판단 샘플입니다.
 */
public class ExsRetryEducationSample {

    public boolean retryable(String httpStatus, String errorCode) {
        return "503".equals(httpStatus) || "TIMEOUT".equals(errorCode);
    }
}
