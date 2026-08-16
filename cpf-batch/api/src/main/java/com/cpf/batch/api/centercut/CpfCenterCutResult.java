package com.cpf.batch.api.centercut;

/**
 * center-cut 단일 대상 처리 결과입니다.
 *
 * @param targetId             대상 식별자
 * @param status               처리 결과 상태
 * @param message              운영자 확인용 결과 메시지
 * @param resultPayload        업무 결과 payload
 * @param transactionSegmentId 해당 item 실행 segment ID
 */
public record CpfCenterCutResult(
        String targetId,
        CpfCenterCutStatus status,
        String message,
        String resultPayload,
        String transactionSegmentId) {

    public CpfCenterCutResult {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("center-cut result targetId는 필수입니다.");
        }
        status = status == null ? CpfCenterCutStatus.SUCCESS : status;
    }

    public static CpfCenterCutResult success(
            CpfCenterCutTarget target,
            String message,
            String resultPayload) {
        return success(target, message, resultPayload, target.transactionSegmentId());
    }

    public static CpfCenterCutResult success(
            CpfCenterCutTarget target,
            String message,
            String resultPayload,
            String transactionSegmentId) {
        return new CpfCenterCutResult(
                target.targetId(),
                CpfCenterCutStatus.SUCCESS,
                message,
                resultPayload,
                transactionSegmentId);
    }

    public static CpfCenterCutResult unknown(CpfCenterCutTarget target, String message, String resultPayload) {
        return new CpfCenterCutResult(target.targetId(), CpfCenterCutStatus.UNKNOWN_RESULT, message, resultPayload, target.transactionSegmentId());
    }

    public static CpfCenterCutResult failed(
            CpfCenterCutTarget target,
            String message,
            String resultPayload) {
        return failed(target, message, resultPayload, target.transactionSegmentId());
    }

    public static CpfCenterCutResult failed(
            CpfCenterCutTarget target,
            String message,
            String resultPayload,
            String transactionSegmentId) {
        return new CpfCenterCutResult(
                target.targetId(),
                CpfCenterCutStatus.FAILED,
                message,
                resultPayload,
                transactionSegmentId);
    }
}
