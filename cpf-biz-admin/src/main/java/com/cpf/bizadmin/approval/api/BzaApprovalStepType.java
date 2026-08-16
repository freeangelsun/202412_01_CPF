package com.cpf.bizadmin.approval.api;

/** BZA 업무결재 단계의 업무 의미입니다. */
public enum BzaApprovalStepType {
    /** 승인권자의 승인. */ APPROVAL,
    /** 부서/조직/전문가의 합의. */ AGREEMENT,
    /** 의사결정권 없이 검토 사실을 남기는 단계. */ REVIEW
}
