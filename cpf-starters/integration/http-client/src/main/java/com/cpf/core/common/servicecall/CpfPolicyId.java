package com.cpf.core.common.servicecall;

/**
 * 중앙 정책 레지스트리에서 해석하는 CPF 호출 정책 식별자입니다.
 *
 * @since 1.0.0
 */
public enum CpfPolicyId {
    /** 일반 조회의 제품 기본 정책입니다. */
    DEFAULT_QUERY,
    /** 짧은 응답시간이 필요한 승인된 조회 정책입니다. */
    FAST_READ,
    /** 장시간 실행 승인이 필요한 제한 정책입니다. */
    LONG_RUNNING_APPROVED
}
