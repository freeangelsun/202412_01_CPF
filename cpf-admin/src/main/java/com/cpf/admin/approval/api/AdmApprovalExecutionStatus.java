package com.cpf.admin.approval.api;

/**
 * 승인 완료 후 Owner Command 실행 상태입니다.
 *
 * <p>{@link #UNKNOWN}은 실패와 다르며 재시도 전에 Owner 결과 조회/Reconciliation이 필요합니다.</p>
 */
public enum AdmApprovalExecutionStatus {
    /** 승인 완료 후 아직 Owner Command 실행 전입니다. */
    PENDING,
    /** Owner Command 실행을 시작했습니다. */
    RUNNING,
    /** Owner가 성공을 확정했습니다. */
    SUCCEEDED,
    /** Owner가 실패를 확정했습니다. */
    FAILED,
    /** Timeout/통신단절 등으로 결과를 단정할 수 없어 조회·대사가 필요합니다. */
    UNKNOWN,
    /** UNKNOWN/부분 실패가 대사·복구 절차로 확정 처리되었습니다. */
    RECOVERED
}
