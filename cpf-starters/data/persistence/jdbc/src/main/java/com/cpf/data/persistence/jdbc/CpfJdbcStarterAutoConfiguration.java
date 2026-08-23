package com.cpf.data.persistence.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import com.cpf.data.persistence.api.database.CpfNamedParameterJdbcOperations;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * CPF JDBC Public Starter의 표준 {@code DataSource}/Transaction 연동을 구성합니다.
 * <p>애플리케이션이 JDBC capability를 선택했을 때만 사용하며, CPF 공용 JDBC 작업 계약과
 * readiness 검증을 Spring Bean으로 연결합니다. 업무 코드는 이 자동구성 클래스를 직접 호출하지 않습니다.
 */
@AutoConfiguration(afterName = "com.cpf.core.config.CpfDataSourceConfig")
@EnableConfigurationProperties(CpfJdbcStarterProperties.class)
public class CpfJdbcStarterAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnSingleCandidate(DataSource.class)
    static class CpfJdbcOperationsConfiguration {
        @Bean
        @ConditionalOnSingleCandidate(PlatformTransactionManager.class)
        @ConditionalOnMissingBean(CpfNamedParameterJdbcOperations.class)
        CpfNamedParameterJdbcOperations cpfJdbcOperations(
                DataSource dataSource,
                PlatformTransactionManager transactionManager) {
            return new CpfSpringJdbcOperations(
                    new NamedParameterJdbcTemplate(dataSource),
                    new TransactionTemplate(transactionManager));
        }
    }

    @Bean
    SmartInitializingSingleton cpfJdbcReadinessVerifier(CpfJdbcStarterProperties properties, ObjectProvider<DataSource> dataSources) {
        return () -> {
            properties.validate();
            if (!properties.isEnabled()) return;
            java.util.List<DataSource> candidates = dataSources.orderedStream().toList();
            if (candidates.isEmpty()) {
                if (properties.isRequired()) throw new IllegalStateException("CPF JDBC starter is enabled but no DataSource exists");
                return;
            }
            for (DataSource dataSource : candidates) validate(dataSource, properties);
        };
    }

    @Bean("cpfJdbcHealthIndicator")
    HealthIndicator cpfJdbcHealthIndicator(CpfJdbcStarterProperties properties, ObjectProvider<DataSource> dataSources) {
        return () -> {
            if (!properties.isEnabled()) return Health.unknown().withDetail("enabled", false).build();
            java.util.List<DataSource> candidates = dataSources.orderedStream().toList();
            if (candidates.isEmpty()) return Health.down().withDetail("error", "missing DataSource").build();
            try {
                for (DataSource dataSource : candidates) validate(dataSource, properties);
                return Health.up().withDetail("dataSourceCount", candidates.size()).build();
            } catch (RuntimeException ex) {
                return Health.down(ex).withDetail("dataSourceCount", candidates.size()).build();
            }
        };
    }

    private static void validate(DataSource dataSource, CpfJdbcStarterProperties properties) {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(properties.getValidationTimeoutSeconds());
            statement.execute(properties.getValidationQuery());
        } catch (Exception ex) {
            throw new IllegalStateException("CPF JDBC readiness validation failed", ex);
        }
    }
}
