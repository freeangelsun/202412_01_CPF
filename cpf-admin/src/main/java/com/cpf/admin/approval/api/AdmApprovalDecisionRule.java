package com.cpf.admin.approval.api;

/**
 * ADM 위험조치 승인 단계의 완료 규칙입니다.
 *
 * <p>DB 문자열을 임의 비교하지 않고 Public/Application 계약에서 같은 의미를 사용하기 위한
 * 최소 Enum입니다. 실제 승인 Engine과 DB Repository는 {@code cpf-admin}이 소유합니다.</p>
 */
public enum AdmApprovalDecisionRule {
    /** 모든 해석된 참여자가 승인해야 합니다. */
    ALL,
    /** 한 명 이상의 승인으로 단계를 완료합니다. */
    ANY,
    /** 정책의 requiredCount 이상 승인이 필요합니다. */
    N_OF_M
}
