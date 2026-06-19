package cpf.pfw.common.batch;

/**
 * CPF 배치 운영 요청 유형입니다.
 */
public enum CpfBatchOperationType {
    RUN,
    SCHEDULE_RUN,
    RETRY,
    STOP
}
