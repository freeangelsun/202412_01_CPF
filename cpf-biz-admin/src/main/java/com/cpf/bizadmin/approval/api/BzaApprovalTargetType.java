package com.cpf.bizadmin.approval.api;

/**
 * BZA 결재 정책에서 실제 결재자/합의자 후보를 해석하는 Target 유형입니다.
 *
 * <p>정책 저장 시에는 동적 Target을 유지하고, Approval Instance 생성 시점에 Directory를
 * 해석하여 실제 참여자와 조직/직급/직책 Snapshot을 고정해야 합니다.</p>
 */
public enum BzaApprovalTargetType {
    /** 특정 직원 한 명을 직접 지정합니다. */
    EMPLOYEE,
    /** 유효한 BZA Role을 가진 직원 집합을 해석합니다. */
    ROLE,
    /** 특정 조직의 유효 소속 직원을 해석합니다. */
    ORGANIZATION,
    /** 특정 조직의 유효 책임자를 해석합니다. */
    ORG_MANAGER,
    /** 특정 직급을 가진 유효 소속 직원을 해석합니다. */
    POSITION
}
