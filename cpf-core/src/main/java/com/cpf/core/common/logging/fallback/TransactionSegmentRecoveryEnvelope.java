package com.cpf.core.common.logging.fallback;

import com.cpf.core.common.logging.segment.TransactionSegmentRecord;

import java.time.Instant;

/**
 * 거래 구간 시작·종료 DB 저장 실패를 재현 가능한 순서로 보존하는 journal 레코드입니다.
 */
public record TransactionSegmentRecoveryEnvelope(
        String recoveryEventId,
        int eventVersion,
        String eventType,
        int sequenceNo,
        String transactionGlobalId,
        String transactionSegmentId,
        String parentSegmentId,
        int attemptCount,
        Instant firstFailedAt,
        Instant nextAttemptAt,
        String lastFailureType,
        String claimedBy,
        Instant claimedAt,
        TransactionSegmentRecord record) {

    public TransactionSegmentRecoveryEnvelope claimed(String workerId, Instant claimedTime) {
        return new TransactionSegmentRecoveryEnvelope(
                recoveryEventId,
                eventVersion,
                eventType,
                sequenceNo,
                transactionGlobalId,
                transactionSegmentId,
                parentSegmentId,
                attemptCount,
                firstFailedAt,
                nextAttemptAt,
                lastFailureType,
                workerId,
                claimedTime,
                record);
    }

    public TransactionSegmentRecoveryEnvelope retry(int attempts, Instant retryAt, String failureType) {
        return new TransactionSegmentRecoveryEnvelope(
                recoveryEventId,
                eventVersion,
                eventType,
                sequenceNo,
                transactionGlobalId,
                transactionSegmentId,
                parentSegmentId,
                attempts,
                firstFailedAt,
                retryAt,
                failureType,
                null,
                null,
                record);
    }
}
