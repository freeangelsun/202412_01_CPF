package com.cpf.batch.api.centercut;

import java.time.LocalDate;

/**
 * center-cut으로 처리할 단일 업무 대상입니다.
 *
 * <p>CPF 2.x 식별 정책에서는 하나의 업무 흐름 전체가 {@code transactionId} 하나를 승계하고,
 * center-cut item 실행 구간만 {@code transactionSegmentId}/{@code parentSegmentId}로 구분합니다.</p>
 *
 * @param targetId             대상 식별자
 * @param centerCutJobId       center-cut Job ID
 * @param businessKey          업무 멱등성 판단 키
 * @param businessDate         업무 기준일
 * @param payload              업무 처리 입력 payload
 * @param transactionId        업무 흐름 전체가 승계하는 CPF transactionId
 * @param parentSegmentId      현재 item 실행 구간의 부모 segment ID
 * @param transactionSegmentId 현재 item 실행 segment ID
 * @param retryCount           재처리 횟수
 * @param status               현재 상태
 */
public record CpfCenterCutTarget(
        String targetId,
        String centerCutJobId,
        String businessKey,
        LocalDate businessDate,
        String payload,
        String transactionId,
        String parentSegmentId,
        String transactionSegmentId,
        int retryCount,
        CpfCenterCutStatus status) {

    public CpfCenterCutTarget {
        if (!hasText(targetId)) {
            throw new IllegalArgumentException("center-cut targetId는 필수입니다.");
        }
        if (!hasText(centerCutJobId)) {
            throw new IllegalArgumentException("center-cut jobId는 필수입니다.");
        }
        if (!hasText(businessKey)) {
            throw new IllegalArgumentException("center-cut businessKey는 필수입니다.");
        }
        status = status == null ? CpfCenterCutStatus.READY : status;
    }

    public CpfCenterCutTarget withExecutionContext(
            String transactionId,
            String parentSegmentId,
            String transactionSegmentId) {
        return new CpfCenterCutTarget(
                targetId,
                centerCutJobId,
                businessKey,
                businessDate,
                payload,
                transactionId,
                parentSegmentId,
                transactionSegmentId,
                retryCount,
                status);
    }

    public CpfCenterCutTarget withStatus(CpfCenterCutStatus value) {
        return new CpfCenterCutTarget(
                targetId,
                centerCutJobId,
                businessKey,
                businessDate,
                payload,
                transactionId,
                parentSegmentId,
                transactionSegmentId,
                retryCount,
                value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
