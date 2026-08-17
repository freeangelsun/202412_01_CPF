package com.cpf.data.persistence.mybatis;

import com.cpf.data.persistence.api.database.CpfSqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Exposes the provider-neutral CPF data operations over the CPF MyBatis session. */
@AutoConfiguration(afterName = "com.cpf.data.persistence.mybatis.config.CpfMyBatisConfig")
public class CpfMyBatisDataOperationsAutoConfiguration {
    @Bean
    @ConditionalOnBean(name = {"cpfSqlSessionTemplate", "cpfTransactionManager"})
    @ConditionalOnMissingBean(CpfSqlSession.class)
    CpfSqlSession cpfDataOperations(
            @Qualifier("cpfSqlSessionTemplate") SqlSessionTemplate sessions,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager transactionManager) {
        return new CpfMyBatisDataOperations(sessions, new TransactionTemplate(transactionManager));
    }
}
