package fps.pfw.common.workflow;

/**
 * 분산 거래/워크플로우 관점의 처리 상태입니다.
 * TRAN_LOG 목록 화면과 TRAN_LOG_DTL 상세 화면에서 동일한 값으로 조회합니다.
 */
public enum FpsWorkflowStatus {
    NONE,
    STARTED,
    COMPLETED,
    FAILED,
    RETRY_PENDING,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED,
    PENDING,
    VERIFY_REQUIRED,
    MANUAL_REQUIRED,
    IGNORED
}
