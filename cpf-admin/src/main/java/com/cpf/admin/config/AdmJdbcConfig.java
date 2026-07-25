package com.cpf.admin.config;

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

/**
 * ADM 소유 DB만 구성합니다.
 *
 * <p>BAT/MBR/REF 등 다른 Owner의 DataSource/JdbcTemplate/TransactionManager는 ADM에 만들지 않습니다.
 * Owner 데이터는 각 Owner의 Public API/Operations Port를 통해 조회·변경합니다.</p>
 */
@Configuration
public class AdmJdbcConfig {
    @Bean(name = "admDataSource")
    public DataSource admDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "spring.datasource.adm");
    }

    @Bean(name = "admTransactionManager")
    public PlatformTransactionManager admTransactionManager(@Qualifier("admDataSource") DataSource admDataSource) {
        return new DataSourceTransactionManager(admDataSource);
    }

    @Bean(name = "admJdbcTemplate")
    public JdbcTemplate admJdbcTemplate(@Qualifier("admDataSource") DataSource admDataSource) {
        return new JdbcTemplate(admDataSource);
    }
}
