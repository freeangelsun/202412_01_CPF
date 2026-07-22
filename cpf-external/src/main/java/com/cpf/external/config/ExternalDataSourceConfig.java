package com.cpf.external.config;

import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.JdbcCpfReconciliationRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/** EXS 업무 DB와 CPF 복구 원장을 서로 다른 최소 권한 계정으로 연결합니다. */
@Configuration
public class ExternalDataSourceConfig {

    @Bean(name = "exsDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.exs")
    public DataSource exsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "exsJdbcTemplate")
    @Primary
    public JdbcTemplate exsJdbcTemplate(@Qualifier("exsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "exsTransactionManager")
    public PlatformTransactionManager exsTransactionManager(
            @Qualifier("exsDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cpfReconciliationDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.cpf")
    public DataSource cpfReconciliationDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "cpfReconciliationJdbcTemplate")
    public JdbcTemplate cpfReconciliationJdbcTemplate(
            @Qualifier("cpfReconciliationDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /** 결과 불명 원장은 EXS 테이블이 아니라 CPF 공통 복구 계약을 통해 기록합니다. */
    @Bean
    @ConditionalOnMissingBean(CpfReconciliationPort.class)
    public CpfReconciliationPort externalReconciliationPort(
            @Qualifier("cpfReconciliationJdbcTemplate") JdbcTemplate jdbcTemplate) {
        return new JdbcCpfReconciliationRepository(jdbcTemplate);
    }
}
