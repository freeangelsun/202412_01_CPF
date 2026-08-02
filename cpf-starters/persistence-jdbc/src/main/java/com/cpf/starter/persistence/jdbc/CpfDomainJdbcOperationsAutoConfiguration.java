
package com.cpf.starter.persistence.jdbc;

import com.cpf.core.api.database.CpfJdbcOperations;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Assembles CPF JDBC operations only when a Generated Domain selected the JDBC provider. */
@AutoConfiguration(after = CpfDomainDataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "cpf.domain.persistence", name = "provider", havingValue = "jdbc")
@ConditionalOnBean(name = {"cpfDomainDataSource", "cpfDomainTransactionManager"})
public class CpfDomainJdbcOperationsAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfJdbcOperations.class)
    CpfJdbcOperations cpfJdbcOperations(
            @Qualifier("cpfDomainDataSource") DataSource dataSource,
            @Qualifier("cpfDomainTransactionManager") PlatformTransactionManager transactionManager) {
        return new CpfSpringJdbcOperations(
                new NamedParameterJdbcTemplate(dataSource), new TransactionTemplate(transactionManager));
    }
}
