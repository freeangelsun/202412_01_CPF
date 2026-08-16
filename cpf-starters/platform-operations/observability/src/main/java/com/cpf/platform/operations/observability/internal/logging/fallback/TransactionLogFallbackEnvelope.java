package com.cpf.platform.operations.observability.internal.logging.fallback;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

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
        LogPolicyDecision logPolicy,
        String claimToken) {

    /** Backward-compatible constructor for journals written before claim-token fencing. */
    public TransactionLogFallbackEnvelope(
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
        this(recoveryEventId, attemptCount, firstFailedAt, nextAttemptAt, lastFailureType,
                claimedBy, claimedAt, record, details, logPolicy, null);
    }

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
                claimedBy,
                claimedAt,
                record,
                details,
                logPolicy,
                claimToken);
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
                logPolicy,
                UUID.randomUUID().toString());
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
                logPolicy,
                null);
    }
}
