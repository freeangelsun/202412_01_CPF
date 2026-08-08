package com.cpf.starter.messaging.reliability.jdbc;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.starter.messaging.reliability.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.starter.messaging.reliability.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.core.common.broker.CpfBrokerPublishResultProbe;
import com.cpf.core.common.broker.CpfBrokerPublisher;
import com.cpf.starter.messaging.reliability.jdbc.internal.CpfBrokerPublisherWorker;
import com.cpf.starter.messaging.reliability.jdbc.internal.JdbcCpfBrokerReliabilityRepository;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration
@EnableConfigurationProperties(CpfMessagingReliabilityProperties.class)
@ConditionalOnProperty(prefix = "cpf.messaging.reliability", name = "enabled", havingValue = "true", matchIfMissing = true)
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
    @ConditionalOnMissingBean(CpfBrokerPublisher.class)
    CpfBrokerPublisher cpfProviderBrokerPublisher(
            CpfBrokerClientRouter router,
            @Qualifier("cpfBrokerClock") Clock clock) {
        return new CpfProviderBrokerPublisher(router, clock);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cpfReliableBrokerClient")
    CpfBrokerClient cpfReliableBrokerClient(
            JdbcCpfBrokerReliabilityRepository repository,
            @Qualifier("cpfBrokerClock") Clock clock) {
        return new CpfReliableBrokerClient(repository, clock);
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
