package com.cpf.starter.runtime;

/**
 * Capability별 공개 Binding 선택 규칙입니다.
 * Provider 자체의 단일성 규칙과 논리 Resource/Client의 다중 Binding 규칙을 분리합니다.
 */
public enum CpfCapabilityBindingCardinality {
    /** 반드시 정확히 하나의 기본 Binding이 있어야 합니다. */
    SINGLE_DEFAULT_REQUIRED,
    /** 여러 Named Binding을 허용하고 기본 Binding은 0개 또는 1개만 허용합니다. */
    NAMED_MULTI_OPTIONAL_DEFAULT,
    /** 모든 호출자가 Binding 이름을 명시해야 하며 기본 Binding을 허용하지 않습니다. */
    EXPLICIT_ONLY,
    /** 외부 공개 Binding 선택 대상이 아닌 내부 Capability입니다. */
    INTERNAL_NO_PUBLIC_BINDING
}
