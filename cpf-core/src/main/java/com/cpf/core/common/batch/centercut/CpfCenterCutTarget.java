package cpf.pfw.common.batch.centercut;

import java.time.LocalDate;

/**
 * center-cut으로 처리할 단일 업무 대상입니다.
 *
 * @param targetId                  대상 식별자
 * @param centerCutJobId            center-cut Job ID
 * @param businessKey               업무 멱등성 판단 키
 * @param businessDate              업무 기준일
 * @param payload                   업무 처리 입력 payload
 * @param parentTransactionGlobalId 부모 거래 글로벌 ID
 * @param childTransactionGlobalId  자식 거래 글로벌 ID
 * @param retryCount                재처리 횟수
 * @param status                    현재 상태
 */
public record CpfCenterCutTarget(
        String targetId,
        String centerCutJobId,
        String businessKey,
        LocalDate businessDate,
        String payload,
        String parentTransactionGlobalId,
        String childTransactionGlobalId,
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

    public CpfCenterCutTarget withChildTransactionGlobalId(String value) {
        return new CpfCenterCutTarget(
                targetId,
                centerCutJobId,
                businessKey,
                businessDate,
                payload,
                parentTransactionGlobalId,
                value,
                retryCount,
                status);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
