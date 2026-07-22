package com.cpf.reference.config;

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
 * REF 업무 DB 연결을 구성합니다.
 *
 * <p>REF는 교육 모듈이지만 신규 업무 모듈이 자기 업무 DB를 붙이는 기준을 보여줘야 하므로
 * CMN/CPF datasource와 별도로 refDB datasource를 둡니다.</p>
 */
@Configuration
public class ReferenceDataSourceConfig {
    @Bean(name = "refDataSource")
    public DataSource refDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.ref");
    }

    @Bean(name = "refJdbcTemplate")
    public JdbcTemplate refJdbcTemplate(@Qualifier("refDataSource") DataSource refDataSource) {
        return new JdbcTemplate(refDataSource);
    }

    @Bean(name = "refTransactionManager")
    public PlatformTransactionManager refTransactionManager(@Qualifier("refDataSource") DataSource refDataSource) {
        return new DataSourceTransactionManager(refDataSource);
    }
}
