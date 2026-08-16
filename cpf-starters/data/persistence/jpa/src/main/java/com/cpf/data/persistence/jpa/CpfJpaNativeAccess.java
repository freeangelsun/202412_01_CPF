package com.cpf.data.persistence.jpa;

import jakarta.persistence.EntityManager;

/** 복잡 JPQL/Criteria/EntityGraph/Vendor 기능이 필요한 경우 사용하는 명시적 native escape입니다. */
public interface CpfJpaNativeAccess {
    EntityManager entityManager();

    default <T> T unwrap(Class<T> providerType) {
        return entityManager().unwrap(providerType);
    }
}
