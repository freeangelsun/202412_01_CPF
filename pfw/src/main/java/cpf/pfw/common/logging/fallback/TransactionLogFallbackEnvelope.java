package cpf.pfw.common.logging.fallback;

import cpf.pfw.common.logging.TransactionLogRecord;
import cpf.pfw.common.logging.policy.LogPolicyDecision;

import java.time.Instant;
import java.util.Map;

/**
 * DB 거래 로그 재적재에 필요한 최소 정보를 담는 durable journal 레코드입니다.
 */
public record TransactionLogFallbackEnvelope(
        String recoveryEventId,
        int attemptCount,
        Instant firstFailedAt,
        Instant nextAttemptAt,
        String lastFailureType,
        TransactionLogRecord record,
        Map<String, String> details,
        LogPolicyDecision logPolicy) {

    public TransactionLogFallbackEnvelope nextAttempt(
            int nextAttemptCount,
            Instant retryAt,
            String failureType) {
        return new TransactionLogFallbackEnvelope(
                recoveryEventId,
                nextAttemptCount,
                firstFailedAt,
                retryAt,
                failureType,
                record,
                details,
                logPolicy);
    }
}
