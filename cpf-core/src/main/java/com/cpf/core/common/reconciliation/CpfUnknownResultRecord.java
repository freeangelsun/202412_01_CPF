package com.cpf.core.common.reconciliation;

import java.time.Instant;

/**
 * 외부 호출, broker, 파일 전송, 배치에서 결과를 확정하지 못한 경우의 공통 기록입니다.
 */
public record CpfUnknownResultRecord(
        String unknownId,
        String unknownType,
        String unknownStatus,
        String transactionGlobalId,
        String segmentId,
        String externalKey,
        String failureCode,
        String failureMessage,
        String nextAction,
        Instant detectedAt,
        Instant resolvedAt) {

    public CpfUnknownResultRecord {
        if (unknownType == null || unknownType.isBlank()) {
            throw new IllegalArgumentException("unknownType은 필수입니다.");
        }
        unknownStatus = unknownStatus == null || unknownStatus.isBlank()
                ? "CHECK_PENDING"
                : unknownStatus.trim().toUpperCase();
        detectedAt = detectedAt == null ? Instant.now() : detectedAt;
    }
}
