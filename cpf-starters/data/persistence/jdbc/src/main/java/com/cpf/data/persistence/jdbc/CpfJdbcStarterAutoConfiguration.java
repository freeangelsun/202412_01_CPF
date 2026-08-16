package com.cpf.data.persistence.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import com.cpf.data.persistence.api.database.CpfJdbcOperations;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(afterName = "com.cpf.core.config.CpfDataSourceConfig")
@EnableConfigurationProperties(CpfJdbcStarterProperties.class)
public class CpfJdbcStarterAutoConfiguration {
    @Bean
    @ConditionalOnBean({DataSource.class, PlatformTransactionManager.class})
    @ConditionalOnMissingBean(CpfJdbcOperations.class)
    CpfJdbcOperations cpfJdbcOperations(
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
            DataSource dataSource = dataSources.getIfAvailable();
            if (dataSource == null) {
                if (properties.isRequired()) throw new IllegalStateException("CPF JDBC starter is enabled but no DataSource exists");
                return;
            }
            validate(dataSource, properties);
        };
    }

    @Bean("cpfJdbcHealthIndicator")
    HealthIndicator cpfJdbcHealthIndicator(CpfJdbcStarterProperties properties, ObjectProvider<DataSource> dataSources) {
        return () -> {
            if (!properties.isEnabled()) return Health.unknown().withDetail("enabled", false).build();
            DataSource dataSource = dataSources.getIfAvailable();
            if (dataSource == null) return Health.down().withDetail("error", "missing DataSource").build();
            try { validate(dataSource, properties); return Health.up().build(); }
            catch (RuntimeException ex) { return Health.down(ex).build(); }
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
