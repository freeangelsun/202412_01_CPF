package cpf.pfw.common.batch.centercut;

/**
 * center-cut 단일 대상 처리 결과입니다.
 *
 * @param targetId                 대상 식별자
 * @param status                   처리 결과 상태
 * @param message                  운영자 확인용 결과 메시지
 * @param resultPayload            업무 결과 payload
 * @param childTransactionGlobalId 자식 거래 글로벌 ID
 */
public record CpfCenterCutResult(
        String targetId,
        CpfCenterCutStatus status,
        String message,
        String resultPayload,
        String childTransactionGlobalId) {

    public CpfCenterCutResult {
        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException("center-cut result targetId는 필수입니다.");
        }
        status = status == null ? CpfCenterCutStatus.SUCCESS : status;
    }

    public static CpfCenterCutResult success(
            CpfCenterCutTarget target,
            String message,
            String resultPayload,
            String childTransactionGlobalId) {
        return new CpfCenterCutResult(
                target.targetId(),
                CpfCenterCutStatus.SUCCESS,
                message,
                resultPayload,
                childTransactionGlobalId);
    }

    public static CpfCenterCutResult failed(
            CpfCenterCutTarget target,
            String message,
            String resultPayload,
            String childTransactionGlobalId) {
        return new CpfCenterCutResult(
                target.targetId(),
                CpfCenterCutStatus.FAILED,
                message,
                resultPayload,
                childTransactionGlobalId);
    }
}
