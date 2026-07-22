package com.cpf.core.common.batch;

/**
 * 배치 실행 흐름에서 알림이나 운영 로그로 전파할 수 있는 이벤트 유형입니다.
 */
public enum CpfBatchEventType {
    RUN_REQUESTED,
    RUN_COMPLETED,
    RUN_FAILED,
    RETRY_REQUESTED,
    STOP_REQUESTED,
    EXECUTION_DELAYED,
    EXECUTION_NOT_RUN
}
