package com.cpf.batch.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * BAT가 소유하는 Spring Batch 메타와 배치 런타임 상태의 DB 연결을 구성합니다.
 */
@Configuration
public class BatDataSourceConfig {

    @Bean(name = "batDataSource")
    public DataSource batDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.bat");
    }

    @Bean(name = "batTransactionManager")
    public PlatformTransactionManager batTransactionManager(
            @Qualifier("batDataSource") DataSource batDataSource) {
        return new DataSourceTransactionManager(batDataSource);
    }

    @Bean(name = "batJdbcTemplate")
    public JdbcTemplate batJdbcTemplate(@Qualifier("batDataSource") DataSource batDataSource) {
        return new JdbcTemplate(batDataSource);
    }
}
