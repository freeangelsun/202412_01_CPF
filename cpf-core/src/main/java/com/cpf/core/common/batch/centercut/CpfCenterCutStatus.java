package com.cpf.core.common.batch.centercut;

/**
 * center-cut 대상과 처리 결과가 공통으로 사용하는 상태 코드입니다.
 */
public enum CpfCenterCutStatus {
    READY,
    RUNNING,
    SUCCESS,
    FAILED,
    SKIPPED,
    RETRY_REQUESTED,
    STOP_REQUESTED
}
