package com.cpf.core.common.logging.fallback;

import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.common.logging.policy.LogPolicyDecision;

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
        String claimedBy,
        Instant claimedAt,
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
                null,
                null,
                record,
                details,
                logPolicy);
    }

    public TransactionLogFallbackEnvelope claimed(String workerId, Instant claimedTime) {
        return new TransactionLogFallbackEnvelope(
                recoveryEventId,
                attemptCount,
                firstFailedAt,
                nextAttemptAt,
                lastFailureType,
                workerId,
                claimedTime,
                record,
                details,
                logPolicy);
    }

    public TransactionLogFallbackEnvelope released(Instant retryAt, String failureType) {
        return new TransactionLogFallbackEnvelope(
                recoveryEventId,
                attemptCount,
                firstFailedAt,
                retryAt,
                failureType,
                null,
                null,
                record,
                details,
                logPolicy);
    }
}
