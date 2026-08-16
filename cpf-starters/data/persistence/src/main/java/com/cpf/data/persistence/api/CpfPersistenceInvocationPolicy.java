package com.cpf.data.persistence.api;

import com.cpf.core.api.context.CpfContexts;

/** Persistence Provider Runtime이 공통으로 사용하는 Context fail-closed 정책입니다. */
public final class CpfPersistenceInvocationPolicy {
    private CpfPersistenceInvocationPolicy() { }

    public static void requireManagedContext(Class<?> daoType) {
        if (CpfContexts.current() == null) {
            throw new IllegalStateException("Managed @CpfDao call has no bound CPF Context: " + daoType.getName());
        }
    }
}
