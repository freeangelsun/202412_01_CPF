package com.cpf.bizadmin.approval.api;

/** BZA 업무결재 Target의 완료 규칙입니다. */
public enum BzaApprovalDecisionRule {
    /** Target에서 해석된 모든 참여자의 동의가 필요합니다. */ ALL,
    /** Target에서 해석된 한 명 이상의 동의가 필요합니다. */ ANY,
    /** Target에서 해석된 참여자 중 정책의 requiredCount 이상 동의가 필요합니다. */ N_OF_M
}
