package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.jdbc.CpfDataSources;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/** Generated Domain이 직접 DataSource/Transaction Bean을 만들지 않도록 하는 표준 Runtime. */
@AutoConfiguration(before = CpfJdbcStarterAutoConfiguration.class)
@EnableConfigurationProperties(CpfDomainPersistenceProperties.class)
@ConditionalOnProperty(prefix = "cpf.domain.persistence", name = "enabled", havingValue = "true")
public class CpfDomainDataSourceAutoConfiguration {
    @Bean("cpfDomainDataSource")
    @ConditionalOnMissingBean(name = "cpfDomainDataSource")
    DataSource cpfDomainDataSource(Environment environment, CpfDomainPersistenceProperties properties)
            throws NamingException {
        return CpfDataSources.resolve(environment, properties.dataSourcePrefix());
    }

    @Bean("cpfDomainTransactionManager")
    @ConditionalOnMissingBean(name = "cpfDomainTransactionManager")
    PlatformTransactionManager cpfDomainTransactionManager(
            @Qualifier("cpfDomainDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
