package com.cpf.core.api.tracking;

/** 한 거래 안에서 Subject가 갖는 역할입니다. 기본 고객 이용 이력은 ACTOR를 사용합니다. */
public enum CpfSubjectRole {
    ACTOR,
    RELATED,
    BENEFICIARY,
    OWNER,
    TARGET
}
