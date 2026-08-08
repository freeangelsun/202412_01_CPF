package com.cpf.starter.data.persistence.jpa;

import com.cpf.core.api.persistence.CpfPersistenceContext;

/** Domain audit model을 강제하지 않으면서 CRUD 경계에 감사 처리를 연결하는 hook입니다. */
public interface CpfJpaAuditHook {
    default void before(String operation, Object entity, CpfPersistenceContext context) { }
    default void after(String operation, Object entity, CpfPersistenceContext context) { }
    static CpfJpaAuditHook noop() { return new CpfJpaAuditHook() { }; }
}
