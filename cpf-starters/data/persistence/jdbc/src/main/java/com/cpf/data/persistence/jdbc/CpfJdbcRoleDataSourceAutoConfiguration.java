package com.cpf.data.persistence.jdbc;

import javax.naming.NamingException;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * CPF logical database role에 사용할 물리 DataSource를 표준 설정으로 조립합니다.
 *
 * <p>애플리케이션이 역할별 Bean을 직접 만들 필요 없이 Starter 설정만으로 등록합니다.
 * JNDI/URL 선택과 Secret 처리는 {@link CpfDataSources}의 동일 계약을 사용합니다.</p>
 */
@AutoConfiguration(before = CpfJdbcDataSourceRoleAutoConfiguration.class)
public class CpfJdbcRoleDataSourceAutoConfiguration {
    private static final String ROOT = "cpf.data.persistence.jdbc.role-datasources.";

    @Bean("cpfPlatformDataSource")
    @ConditionalOnMissingBean(name = "cpfPlatformDataSource")
    @ConditionalOnProperty(prefix = ROOT + "cpf-platform-db", name = "enabled", havingValue = "true")
    DataSource cpfPlatformDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, ROOT + "cpf-platform-db");
    }

    /**
     * CPF Platform role DataSource의 짝이 되는 TransactionManager입니다.
     *
     * <p>역할 DataSource만 있고 TransactionManager가 없으면, 단일
     * {@code PlatformTransactionManager}를 요구하는 Starter 자동구성(Spring Session JDBC 등)이
     * 후보를 찾지 못하거나 여러 후보 중에서 고르지 못해 기동이 실패한다. Platform 운영 데이터의
     * 정본은 CPF Platform 스키마이므로 그 역할의 TransactionManager를 함께 제공한다.</p>
     */
    @Bean("cpfPlatformTransactionManager")
    @ConditionalOnMissingBean(name = "cpfPlatformTransactionManager")
    @ConditionalOnProperty(prefix = ROOT + "cpf-platform-db", name = "enabled", havingValue = "true")
    PlatformTransactionManager cpfPlatformTransactionManager(
            @Qualifier("cpfPlatformDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("cpfCustomerBusinessDataSource")
    @ConditionalOnMissingBean(name = "cpfCustomerBusinessDataSource")
    @ConditionalOnProperty(prefix = ROOT + "customer-business-db", name = "enabled", havingValue = "true")
    DataSource cpfCustomerBusinessDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, ROOT + "customer-business-db");
    }

    @Bean("cpfReferenceFixtureDataSource")
    @ConditionalOnMissingBean(name = "cpfReferenceFixtureDataSource")
    @ConditionalOnProperty(prefix = ROOT + "reference-fixture", name = "enabled", havingValue = "true")
    DataSource cpfReferenceFixtureDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, ROOT + "reference-fixture");
    }
}
