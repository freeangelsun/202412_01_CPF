package com.cpf.data.persistence.jdbc.config;

import com.cpf.data.persistence.jdbc.CpfDataSources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * CMN 단일 샘플 테이블을 검증할 때만 비운영 sample datasource를 활성화합니다. 물리 DB는 현재 CPF Platform DB인 cpfDB를 사용하며 별도 cmnDB나 fixture DB를 생성하지 않습니다.
 *
 * <p>cpf-common의 기본 동작은 DB-less입니다. 이 설정은 개발·EDU·통합 테스트에서
 * 연결, CRUD, paging, 낙관적 잠금과 transaction을 검증하는 명시적 sample profile에만
 * 사용하며 업무 채번이나 공통 업무 원장을 제공하지 않습니다. 제품 Profile에서는 Property가 설정돼도 Bean을 생성하지 않습니다.</p>
 */
@Configuration
@Profile({"edu", "test"})
@ConditionalOnProperty(prefix = "cpf.common.sample-db", name = "enabled", havingValue = "true")
public class CmnSampleDataSourceConfig {

    @Bean(name = "cmnSampleDataSource")
    /** cmnSampleDataSource 작업을 CPF 표준 계약에 따라 수행한다. */
    public DataSource cmnSampleDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "spring.datasource.cmn-sample");
    }

    @Bean(name = "cmnSampleJdbcTemplate")
    public JdbcTemplate cmnSampleJdbcTemplate(@Qualifier("cmnSampleDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "cmnSampleTransactionManager")
    /** cmnSampleTransactionManager 작업을 CPF 표준 계약에 따라 수행한다. */
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
