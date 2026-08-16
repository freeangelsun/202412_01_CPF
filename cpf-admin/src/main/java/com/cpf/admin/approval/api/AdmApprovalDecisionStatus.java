package com.cpf.admin.approval.api;

/**
 * ADM 위험조치 승인 단계의 계산 결과입니다.
 *
 * <p>DB 행 상태와 직접 결합하지 않는 Public/Application 의미값입니다. 실제 요청 상태 전이는
 * {@code cpf-admin}의 Approval Engine이 낙관적 잠금과 멱등성 검증 후 수행해야 합니다.</p>
 */
public enum AdmApprovalDecisionStatus {
    /** 아직 필요한 승인 수를 충족하지 않았고 충족 가능성이 남아 있습니다. */
    WAITING,
    /** 정책의 승인 임계값을 충족했습니다. */
    APPROVED,
    /** 남은 참여자가 모두 승인해도 정책 임계값을 충족할 수 없습니다. */
    REJECTED
}
