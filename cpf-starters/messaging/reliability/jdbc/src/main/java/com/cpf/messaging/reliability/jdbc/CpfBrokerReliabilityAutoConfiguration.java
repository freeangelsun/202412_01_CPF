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

    /**
     * Provider 로 실제 발행하는 Publisher 는 라우팅 Template 이 있을 때만 제공합니다.
     *
     * <p>이 Bean 은 {@code CpfMessagingTemplateRouter} 를 필수로 받는다. 그 Router 는 broker
     * Provider 가 구성된 Runtime 에서만 생성되므로(위 {@code cpfBrokerClientRouter} 주석 참조),
     * 같은 조건을 걸지 않으면 Provider 없는 Runtime 에서 "expected at least 1 bean" 로 기동이
     * 실패한다. 실제로 Router 만 조건부로 바꾼 직후 1-WAS 가 이 Bean 에서 멈췄다.</p>
     *
     * <p>원장 기반 발행 경로({@code cpfReliableBrokerClient})와 UNKNOWN reconcile 은 Provider 와
     * 무관하게 유지되므로 ADM 의 Broker 신뢰성 운영 기능은 그대로 살아 있다.</p>
     */
    @Bean
    @ConditionalOnMissingBean(CpfBrokerPublisher.class)
    @ConditionalOnBean(CpfMessagingTemplateRouter.class)
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

    /**
     * Broker Provider 가 실제로 구성된 Runtime 에서만 라우팅 Template 을 제공합니다.
     *
     * <p>이 Bean 은 {@code CpfNamedBrokerClient} 가 하나도 없으면 생성자에서
     * {@code Messaging capability requires one configured broker Provider} 로 던진다. 그런데
     * broker Provider(JMS/Kafka/RabbitMQ/IBM MQ)는 모두 선택 Provider leaf 이고, ADM 의존 경계는
     * 그것들을 명시적으로 금지한다. 그래서 ADM 을 합성하는 1-WAS 에서는 **만족시킬 방법이 없는
     * 필수 요구**가 되어 기동이 실패했다.</p>
     *
     * <p>Batch 3종(control-plane/scheduler/worker)은 같은 문제를 피하려고
     * {@code cpf.messaging.reliability.enabled=false} 로 capability 전체를 껐다. 1-WAS 에는 그 방법을
     * 쓸 수 없다. ADM 은 Broker 신뢰성 운영 기능(승인 Owner/원장 조회)을 제공하는 mandatory Route 라
     * capability 를 끄면 제품 기능이 사라진다(Harness §26.2).</p>
     *
     * <p>따라서 원장(OUTBOX/INBOX/DLQ)과 ADM 운영 기능은 그대로 두고, **Provider 없이는 의미가 없는
     * 라우팅 Template 만** 조건부로 만든다. 바로 아래 {@code @ConditionalOnBean(CpfBrokerPublisher.class)}
     * 형제 Bean 과 같은 방식이다. Provider 가 없는 Runtime 의 소비자는 이미
     * {@code ObjectProvider.getIfAvailable()} 로 부재를 처리한다
     * (예: {@code BatchRuntimeExecutorRegistry} 는 "capability is not installed" 로 보고한다).</p>
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(CpfNamedBrokerClient.class)
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
