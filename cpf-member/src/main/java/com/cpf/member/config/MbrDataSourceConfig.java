package com.cpf.member.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

@Configuration
public class MbrDataSourceConfig {
    @Bean(name = "mbrDataSource")
    public DataSource mbrDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource");
    }

    @Bean(name = "mbrTransactionManager")
    public PlatformTransactionManager mbrTransactionManager(@Qualifier("mbrDataSource") DataSource mbrDataSource) {
        return new DataSourceTransactionManager(mbrDataSource);
    }
}

