package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/** CPF Data JDBC Provider가 제공하는 논리 Database role 조립입니다. */
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
public class CpfJdbcDataSourceRoleAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(CpfDataSourceRegistry.class)
    CpfDataSourceRegistry cpfDataSourceRegistry(ListableBeanFactory beanFactory, Environment environment) {
        return new CpfJdbcDataSourceRegistry(beanFactory, environment);
    }
}
