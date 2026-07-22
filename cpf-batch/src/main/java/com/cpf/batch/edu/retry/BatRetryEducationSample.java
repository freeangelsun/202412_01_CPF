package cpf.bat.edu.retry;

/**
 * 일시 오류에 대한 retry 정책 샘플입니다.
 */
public class BatRetryEducationSample {

    public RetryDecision decide(int attempt, int maxAttempts, boolean transientFailure) {
        boolean retry = transientFailure && attempt < maxAttempts;
        return new RetryDecision(attempt, maxAttempts, retry, retry ? "RETRY" : "STOP");
    }

    public record RetryDecision(int attempt, int maxAttempts, boolean retry, String action) {
    }
}
