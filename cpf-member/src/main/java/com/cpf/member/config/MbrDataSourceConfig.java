package com.cpf.member.config;

import com.cpf.core.api.database.CpfDataSources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/** MBR Owner DB와 transaction/JdbcTemplate을 MBR 내부에서 소유합니다. */
@Configuration
public class MbrDataSourceConfig {
    @Bean(name = "mbrDataSource")
    public DataSource mbrDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "spring.datasource");
    }

    @Bean(name = "mbrTransactionManager")
    public PlatformTransactionManager mbrTransactionManager(@Qualifier("mbrDataSource") DataSource mbrDataSource) {
        return new DataSourceTransactionManager(mbrDataSource);
    }

    @Bean(name = "mbrJdbcTemplate")
    public JdbcTemplate mbrJdbcTemplate(@Qualifier("mbrDataSource") DataSource mbrDataSource) {
        return new JdbcTemplate(mbrDataSource);
    }
}
