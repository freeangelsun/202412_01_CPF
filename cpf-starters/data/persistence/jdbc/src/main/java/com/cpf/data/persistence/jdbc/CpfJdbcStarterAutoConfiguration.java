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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(afterName = "com.cpf.core.config.CpfDataSourceConfig")
@EnableConfigurationProperties(CpfJdbcStarterProperties.class)
public class CpfJdbcStarterAutoConfiguration {
    @Bean
    @ConditionalOnBean({DataSource.class, PlatformTransactionManager.class})
    @ConditionalOnSingleCandidate(DataSource.class)
    @ConditionalOnSingleCandidate(PlatformTransactionManager.class)
    @ConditionalOnMissingBean(CpfNamedParameterJdbcOperations.class)
    CpfNamedParameterJdbcOperations cpfJdbcOperations(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {
        return new CpfSpringJdbcOperations(
                new NamedParameterJdbcTemplate(dataSource),
                new TransactionTemplate(transactionManager));
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
