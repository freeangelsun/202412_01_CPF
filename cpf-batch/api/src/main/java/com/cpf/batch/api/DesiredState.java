package com.cpf.batch.api;

/**
 * 운영자가 Batch Runtime에 요구한 목표 상태입니다.
 * <p>실제 상태({@link ActualState})와 분리해 저장하며 Agent/Reconciler가 두 상태의 차이를 수렴시킵니다.
 */
public enum DesiredState {
    RUNNING, STOPPED, DRAINING, QUARANTINED, UPGRADING, ROLLING_BACK
}
