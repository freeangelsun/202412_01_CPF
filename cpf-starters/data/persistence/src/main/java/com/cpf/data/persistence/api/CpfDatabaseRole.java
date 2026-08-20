package com.cpf.data.persistence.api;

/** CPF가 사용하는 관계형 Database의 논리 역할입니다. 물리 DB 이름을 업무 Source에 노출하지 않습니다. */
public enum CpfDatabaseRole {
    /** CPF Platform control/product metadata database. 기본 물리 이름은 cpfDB입니다. */
    CPF_PLATFORM_DB,
    /** 고객 업무 transaction database. */
    CUSTOMER_BUSINESS_DB,
    /** 교육/검증 전용 Reference Fixture database. Production 기본 DB로 사용하지 않습니다. */
    REFERENCE_FIXTURE
}
