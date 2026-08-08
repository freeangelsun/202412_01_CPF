package com.cpf.core.config;

import com.cpf.core.common.saga.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/** Saga durable runtime 기본 구성. */
@Configuration(proxyBeanMethods=false)
@ConditionalOnBean(name="cpfJdbcTemplate")
public class CpfSagaAutoConfiguration {
    @Bean @ConditionalOnMissingBean public CpfSagaDefinitionRegistry cpfSagaDefinitionRegistry(){return new CpfSagaDefinitionRegistry();}
    @Bean @ConditionalOnMissingBean public CpfSagaStateStore cpfSagaStateStore(@Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc){return new JdbcCpfSagaStateStore(jdbc);}
    @Bean @ConditionalOnMissingBean public CpfSagaEngine cpfSagaEngine(CpfSagaStateStore store,CpfSagaDefinitionRegistry registry){return new CpfSagaEngine(store,registry);}
    @Bean @ConditionalOnMissingBean public CpfSagaManualRecoveryService cpfSagaManualRecoveryService(CpfSagaStateStore store,CpfSagaDefinitionRegistry registry){return new CpfSagaManualRecoveryService(store,registry);}
}
