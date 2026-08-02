package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.CpfBrokerConsumerRuntimePolicy;
import com.cpf.core.common.broker.CpfBrokerConsumerWorker;
import com.cpf.core.common.broker.CpfBrokerPublishResultProbe;
import com.cpf.core.common.broker.CpfBrokerPublisher;
import com.cpf.core.common.broker.CpfBrokerPublisherWorker;
import com.cpf.core.common.broker.JdbcCpfBrokerReliabilityRepository;
import java.sql.Connection;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@EnableConfigurationProperties(CpfMessagingReliabilityProperties.class)
public class CpfBrokerReliabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean(name = "cpfBrokerClock")
    Clock cpfBrokerClock() {
        return Clock.systemUTC();
    }

    @Bean
    JdbcCpfBrokerReliabilityRepository cpfBrokerReliabilityRepository(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            CpfMessagingReliabilityProperties properties,
            @Qualifier("cpfBrokerClock") Clock clock) {
        properties.validate();
        return new JdbcCpfBrokerReliabilityRepository(jdbc, properties.getLease(), clock);
    }

    @Bean
    CpfBrokerConsumerRuntimePolicy cpfBrokerConsumerRuntimePolicy() {
        return new CpfBrokerConsumerRuntimePolicy();
    }

    @Bean
    CpfBrokerConsumerWorker cpfBrokerConsumerWorker(
            JdbcCpfBrokerReliabilityRepository repository,
            CpfBrokerConsumerRuntimePolicy policy) {
        return new CpfBrokerConsumerWorker(repository, repository, policy);
    }

    @Bean
    @ConditionalOnBean(CpfBrokerPublisher.class)
    CpfBrokerPublisherWorker cpfBrokerPublisherWorker(
            JdbcCpfBrokerReliabilityRepository repository,
            CpfBrokerPublisher publisher,
            @Qualifier("cpfBrokerClock") Clock clock,
            CpfMessagingReliabilityProperties properties) {
        return new CpfBrokerPublisherWorker(
                repository, repository, publisher, clock,
                properties.getUnknownReconcileDelay());
    }

    @Bean
    CpfBrokerUnknownResultReconciler cpfBrokerUnknownResultReconciler(
            JdbcCpfBrokerReliabilityRepository repository,
            ObjectProvider<CpfBrokerPublishResultProbe> probes,
            @Qualifier("cpfBrokerClock") Clock clock,
            CpfMessagingReliabilityProperties properties) {
        return new CpfBrokerUnknownResultReconciler(
                repository, probes.orderedStream().toList(), clock,
                properties.getUnknownReconcileDelay());
    }

    @Bean
    CpfBrokerReliabilityOperations cpfBrokerReliabilityOperations(
            JdbcCpfBrokerReliabilityRepository repository) {
        return new CpfBrokerReliabilityOperations(repository);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfBrokerClientRouter cpfBrokerClientRouter(
            ObjectProvider<CpfNamedBrokerClient> clients) {
        return new CpfBrokerClientRouter(clients.orderedStream().toList());
    }

    @Bean
    SmartInitializingSingleton cpfBrokerSchemaVerifier(
            CpfMessagingReliabilityProperties properties,
            DataSource dataSource) {
        return () -> {
            properties.validate();
            if (!properties.isEnabled() || !properties.isSchemaRequired()) {
                return;
            }
            try (Connection connection = dataSource.getConnection()) {
                for (String table : List.of(
                        "cpf_broker_outbox", "cpf_broker_inbox", "cpf_broker_dlq")) {
                    try (var result = connection.getMetaData().getTables(
                            connection.getCatalog(), null, table, null)) {
                        if (!result.next()) {
                            throw new IllegalStateException("Missing CPF broker table: " + table);
                        }
                    }
                }
            } catch (Exception exception) {
                throw new IllegalStateException(
                        "CPF broker reliability schema verification failed", exception);
            }
        };
    }

    @Bean("cpfBrokerReliabilityHealthIndicator")
    HealthIndicator cpfBrokerReliabilityHealthIndicator(DataSource dataSource) {
        return () -> {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(3)
                        ? Health.up().build()
                        : Health.down().withDetail("reasonCode", "BROKER_DB_INVALID").build();
            } catch (Exception exception) {
                return Health.down()
                        .withDetail("reasonCode", "BROKER_DB_UNAVAILABLE")
                        .build();
            }
        };
    }
}
