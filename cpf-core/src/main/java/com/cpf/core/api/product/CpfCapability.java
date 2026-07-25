package com.cpf.core.api.product;

/** CPF 제품 기능 패키징 단위. Edition 이름과 Runtime 구현을 직접 결합하지 않습니다. */
public enum CpfCapability {
    CORE_RUNTIME,
    GATEWAY,
    ADMIN_CONTROL_PLANE,
    BUSINESS_ADMIN,
    BATCH_WORKER,
    CENTER_CUT,
    EXTERNAL_INTEGRATION,
    MULTI_TENANT,
    DATA_LINEAGE,
    ADVANCED_OBSERVABILITY
}
