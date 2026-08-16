package com.cpf.data.persistence.api;

/** Persistence Provider가 공통으로 사용하는 Repository 구조 검증 정책입니다. Spring Runtime에는 의존하지 않습니다. */
public final class CpfPersistenceStructurePolicy {
    private CpfPersistenceStructurePolicy() { }

    /**
     * @CpfRepository는 Interface Port와 Class 기반 Repository 모두의 단일 공개 개념입니다.
     * Interface 계약의 세부 검증은 중앙 Repository Policy BeanPostProcessor가 담당합니다.
     */
    public static void verifyRepositoryType(Class<?> type) {
        if (type == null) throw new IllegalArgumentException("type is required");
    }
}
