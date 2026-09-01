package com.cpf.messaging.reliability.jdbc;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerReliabilityOperations;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerUnknownResultReconciler;
import com.cpf.messaging.spi.CpfNamedBrokerClient;
import com.cpf.messaging.reliability.api.jdbc.CpfMessagingTemplateRouter;
import com.cpf.messaging.reliability.api.jdbc.CpfMessagingReliabilityProperties;
import com.cpf.messaging.reliability.api.jdbc.CpfProviderBrokerPublisher;
import com.cpf.messaging.reliability.api.jdbc.CpfReliableBrokerClient;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.messaging.reliability.api.jdbc.runtimecontrol.CpfMessagingRuntimeControlAutoConfiguration;
import com.cpf.messaging.spi.broker.CpfBrokerPublishResultProbe;
import com.cpf.messaging.spi.broker.CpfBrokerPublisher;
import com.cpf.messaging.reliability.api.jdbc.CpfBrokerPublisherWorker;
import com.cpf.messaging.reliability.api.jdbc.internal.JdbcCpfBrokerReliabilityRepository;
import java.sql.Connection;
import java.time.Clock;
import java.util.List;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
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

@AutoConfiguration(before = CpfMessagingRuntimeControlAutoConfiguration.class)
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
    @ConditionalOnMissingBean
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
            CpfMessagingTemplateRouter router,
            @Qualifier("cpfBrokerClock") Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        return new CpfProviderBrokerPublisher(router, clock, contextSupport);
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "cpfReliableBrokerClient")
    CpfMessagingTemplate cpfReliableBrokerClient(
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
    CpfMessagingTemplateRouter cpfBrokerClientRouter(
            ObjectProvider<CpfNamedBrokerClient> clients) {
        return new CpfMessagingTemplateRouter(clients.orderedStream().toList());
    }

    // Runtime 에는 업무 Domain DataSource 가 함께 존재하므로 타입만으로 주입하면 후보가
    // 여럿이라 기동이 실패한다. Broker 신뢰성 원장(CPF_BROKER_OUTBOX/INBOX/DLQ)은 CPF
    // Platform DB 에 있으므로 Role 을 명시해 해석한다.
    @Bean
    SmartInitializingSingleton cpfBrokerSchemaVerifier(
            CpfMessagingReliabilityProperties properties,
            CpfDataSourceRegistry dataSources) {
        DataSource dataSource = dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
        return () -> {
            properties.validate();
            if (!properties.isEnabled() || !properties.isSchemaRequired()) {
                return;
            }
            try (Connection connection = dataSource.getConnection()) {
                for (String table : List.of(
                        "CPF_BROKER_OUTBOX", "CPF_BROKER_INBOX", "CPF_BROKER_DLQ")) {
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

    // Runtime 에는 업무 Domain DataSource 가 함께 존재하므로 타입만으로 주입하면 후보가
    // 여럿이라 기동이 실패한다. Broker 신뢰성 원장(CPF_BROKER_OUTBOX/INBOX/DLQ)은 CPF
    // Platform DB 에 있으므로 Role 을 명시해 해석한다.
    @Bean("cpfBrokerReliabilityHealthIndicator")
    HealthIndicator cpfBrokerReliabilityHealthIndicator(CpfDataSourceRegistry dataSources) {
        DataSource dataSource = dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
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
