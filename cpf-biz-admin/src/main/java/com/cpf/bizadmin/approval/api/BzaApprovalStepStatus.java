package com.cpf.bizadmin.approval.api;

/**
 * BZA 업무결재 단계의 집계 결과입니다.
 *
 * <p>APPROVAL과 AGREEMENT의 업무 의미는 별도 Step Type으로 보존하고, 이 값은 다음 단계로
 * 진행 가능한지 판단하는 공통 집계 결과만 표현합니다.</p>
 */
public enum BzaApprovalStepStatus {
    /** 아직 정책 임계값을 충족하지 않았고 완료 가능성이 남아 있습니다. */
    WAITING,
    /** ALL/ANY/N_OF_M 정책 임계값을 충족했습니다. */
    APPROVED,
    /** 남은 참여자가 모두 동의해도 정책 임계값을 충족할 수 없습니다. */
    REJECTED
}
