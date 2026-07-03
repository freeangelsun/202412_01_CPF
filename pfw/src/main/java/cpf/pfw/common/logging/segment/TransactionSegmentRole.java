package cpf.pfw.common.logging.segment;

/**
 * 복합 거래 안에서 현재 구간이 맡는 역할입니다.
 */
public enum TransactionSegmentRole {
    MAIN,
    SUB,
    SHARED,
    EXTERNAL,
    BATCH,
    CENTER_CUT_PARENT,
    CENTER_CUT_CHILD,
    OPERATOR_ACTION
}
