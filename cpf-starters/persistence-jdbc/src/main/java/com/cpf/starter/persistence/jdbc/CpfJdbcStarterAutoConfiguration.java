package com.cpf.starter.persistence.jdbc;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(afterName = "com.cpf.core.config.CpfDataSourceConfig")
@EnableConfigurationProperties(CpfJdbcStarterProperties.class)
public class CpfJdbcStarterAutoConfiguration {
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
