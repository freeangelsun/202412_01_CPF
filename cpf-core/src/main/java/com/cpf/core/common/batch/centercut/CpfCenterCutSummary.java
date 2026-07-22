package com.cpf.core.common.batch.centercut;

/**
 * center-cut 한 번의 실행 요약입니다.
 */
public record CpfCenterCutSummary(
        String centerCutJobId,
        int requestedCount,
        int successCount,
        int failedCount,
        int skippedCount,
        int retryRequestedCount,
        int stopRequestedCount) {

    public static CpfCenterCutSummary empty(String centerCutJobId) {
        return new CpfCenterCutSummary(centerCutJobId, 0, 0, 0, 0, 0, 0);
    }
}
