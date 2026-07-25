package com.cpf.core.api.batch;

/**
 * <p>이 타입은 BAT Runtime 구현과 분리된 CPF 공개 Batch 계약입니다.</p>
 * CPF 배치 운영 요청 유형입니다.
 */
public enum CpfBatchOperationType {
    RUN,
    SCHEDULE_RUN,
    RESTART,
    RERUN,
    /** 기존 호출자 호환용 유형이며 신규 API는 RESTART 또는 RERUN을 명시합니다. */
    RETRY,
    STOP
}
