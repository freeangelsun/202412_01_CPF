package com.cpf.messaging.reliability.jdbc;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerReliabilityOperations;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerUnknownResultReconciler;
import com.cpf.messaging.spi.CpfNamedBrokerClient;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerClientRouter;
import com.cpf.messaging.reliability.api.jdbc.CpfMessagingReliabilityProperties;

import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.messaging.spi.broker.CpfBrokerPublishResultProbe;
import com.cpf.messaging.spi.broker.CpfBrokerPublisher;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerPublisherWorker;
import com.cpf.messaging.reliability.api.jdbc.internal.JdbcCpfBrokerReliabilityRepository;
import java.sql.Connection;
import java.time.Clock;
import java.util.List;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
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
    @ConditionalOnMissingBean
    CpfMessageBridgeContextSupport cpfMessageBridgeContextSupport(CpfExecutionIdGenerator executionIds) { return new CpfMessageBridgeContextSupport(executionIds); }

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
            @Qualifier("cpfBrokerClock") Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        return new CpfProviderBrokerPublisher(router, clock, contextSupport);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cpfReliableBrokerClient")
    CpfBrokerClient cpfReliableBrokerClient(
            JdbcCpfBrokerReliabilityRepository repository,
            @Qualifier("cpfBrokerClock") Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        return new CpfReliableBrokerClient(repository, clock, contextSupport);
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
            JdbcCpfBrokerReliabilityRepository repository,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc) {
        return new CpfBrokerReliabilityOperations(repository, jdbc);
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
