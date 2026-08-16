package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.CpfPersistenceContext;

/** 현재 transaction/tenant/actor context를 JPA Provider에 공급하는 확장점입니다. */
@FunctionalInterface
public interface CpfJpaExecutionContextSupplier {
    CpfPersistenceContext current();

    static CpfJpaExecutionContextSupplier empty() {
        return () -> new CpfPersistenceContext("UNAVAILABLE", null, null);
    }
}
