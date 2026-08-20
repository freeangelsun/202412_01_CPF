package com.cpf.batch.api;

/**
 * Batch Runtime Instance가 실제로 관측된 현재 상태입니다.
 * <p>{@code UNKNOWN}/{@code UNREACHABLE}을 정상 종료로 취급하지 않으며 desired state와 비교해 Reconcile 여부를 판단합니다.
 */
public enum ActualState {
    STARTING, READY, BUSY, DRAINING, STOPPED, DEGRADED, STALE, UNREACHABLE, FAILED, UNKNOWN
}
