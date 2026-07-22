package cpf.pfw.common.logging.segment;

/**
 * 거래 구간의 처리 상태입니다.
 */
public enum TransactionSegmentStatus {
    RUNNING,
    SUCCESS,
    FAILED,
    PARTIAL_FAILED
}
