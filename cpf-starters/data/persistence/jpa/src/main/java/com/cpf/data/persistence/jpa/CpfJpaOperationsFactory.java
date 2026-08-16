package com.cpf.data.persistence.jpa;

import jakarta.persistence.EntityManagerFactory;
import java.util.Objects;
import org.springframework.orm.jpa.SharedEntityManagerCreator;

/** Multi-datasource/DB3에서 각 EntityManagerFactory용 CPF JPA Operations를 명시적으로 생성하는 escape point입니다. */
public final class CpfJpaOperationsFactory {
    private final CpfJpaProperties properties;
    private final CpfJpaQueryObserver observer;
    private final CpfJpaExecutionContextSupplier contexts;
    private final CpfJpaAuditHook auditHook;

    public CpfJpaOperationsFactory(CpfJpaProperties properties, CpfJpaQueryObserver observer,
            CpfJpaExecutionContextSupplier contexts, CpfJpaAuditHook auditHook) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.observer = Objects.requireNonNull(observer, "observer");
        this.contexts = Objects.requireNonNull(contexts, "contexts");
        this.auditHook = Objects.requireNonNull(auditHook, "auditHook");
    }

    public CpfJpaOperations forEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        return new CpfJpaOperations(SharedEntityManagerCreator.createSharedEntityManager(
            Objects.requireNonNull(entityManagerFactory, "entityManagerFactory")), properties, observer, contexts, auditHook);
    }
}
