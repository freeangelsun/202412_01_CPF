package com.cpf.data.persistence.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.jta.JtaTransactionManager;

/** JPA를 실제 선택하고 EntityManagerFactory가 존재할 때만 CPF 편의 Bean을 생성합니다. */
@AutoConfiguration
@ConditionalOnClass({EntityManager.class, EntityManagerFactory.class})
@ConditionalOnBean(EntityManagerFactory.class)
@EnableConfigurationProperties(CpfJpaProperties.class)
public class CpfJpaAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfJpaQueryObserver cpfJpaQueryObserver() { return CpfJpaQueryObserver.noop(); }

    @Bean @ConditionalOnMissingBean
    CpfJpaExecutionContextSupplier cpfJpaExecutionContextSupplier() { return CpfJpaExecutionContextSupplier.empty(); }

    @Bean @ConditionalOnMissingBean
    CpfJpaAuditHook cpfJpaAuditHook() { return CpfJpaAuditHook.noop(); }

    @Bean @ConditionalOnMissingBean
    CpfJpaOperationsFactory cpfJpaOperationsFactory(CpfJpaProperties properties, CpfJpaQueryObserver observer,
            CpfJpaExecutionContextSupplier contexts, CpfJpaAuditHook auditHook,
            ObjectProvider<JtaTransactionManager> jtaTransactionManagers) {
        if (properties.isRequireJta() && jtaTransactionManagers.getIfAvailable() == null) {
            throw new IllegalStateException("cpf.data.persistence.jpa.require-jta=true but no JtaTransactionManager is available");
        }
        return new CpfJpaOperationsFactory(properties, observer, contexts, auditHook);
    }

    @Bean @ConditionalOnMissingBean(CpfJpaOperations.class)
    @ConditionalOnSingleCandidate(EntityManagerFactory.class)
    CpfJpaOperations cpfJpaOperations(EntityManagerFactory emf, CpfJpaOperationsFactory factory) {
        return factory.forEntityManagerFactory(emf);
    }
}
