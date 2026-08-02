package com.cpf.starter.persistence.mybatis;

import com.cpf.core.api.database.CpfDataOperations;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Exposes the provider-neutral CPF data operations over the CPF MyBatis session. */
@AutoConfiguration(afterName = "com.cpf.core.config.CpfMyBatisConfig")
public class CpfMyBatisDataOperationsAutoConfiguration {
    @Bean
    @ConditionalOnBean(name = {"cpfSqlSessionTemplate", "cpfTransactionManager"})
    @ConditionalOnMissingBean(CpfDataOperations.class)
    CpfDataOperations cpfDataOperations(
            @Qualifier("cpfSqlSessionTemplate") SqlSessionTemplate sessions,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager transactionManager) {
        return new CpfMyBatisDataOperations(sessions, new TransactionTemplate(transactionManager));
    }
}
