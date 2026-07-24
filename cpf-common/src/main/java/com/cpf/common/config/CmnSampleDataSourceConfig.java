package com.cpf.common.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * CMN 단일 샘플 테이블을 검증할 때만 cmnDB 연결을 활성화합니다.
 *
 * <p>cpf-common의 기본 동작은 DB-less입니다. 이 설정은 개발·EDU·통합 테스트에서
 * 연결, CRUD, paging, 낙관적 잠금과 transaction을 검증하는 명시적 sample profile에만
 * 사용하며 업무 채번이나 공통 업무 원장을 제공하지 않습니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.cmn.sample-db", name = "enabled", havingValue = "true")
public class CmnSampleDataSourceConfig {

    @Bean(name = "cmnSampleDataSource")
    public DataSource cmnSampleDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cmn-sample");
    }

    @Bean(name = "cmnSampleJdbcTemplate")
    public JdbcTemplate cmnSampleJdbcTemplate(@Qualifier("cmnSampleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "cmnSampleTransactionManager")
    public PlatformTransactionManager cmnSampleTransactionManager(
            @Qualifier("cmnSampleDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cmnSampleTransactionTemplate")
    public TransactionTemplate cmnSampleTransactionTemplate(
            @Qualifier("cmnSampleTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
