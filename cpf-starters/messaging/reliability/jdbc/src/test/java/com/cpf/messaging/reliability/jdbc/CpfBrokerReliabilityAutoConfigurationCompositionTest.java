package com.cpf.messaging.reliability.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerRuntimePolicy;
import com.cpf.messaging.reliability.api.jdbc.internal.CpfBrokerConsumerWorker;
import com.cpf.messaging.reliability.api.jdbc.runtimecontrol.CpfMessagingRuntimeControlAutoConfiguration;
import com.cpf.messaging.spi.CpfNamedBrokerClient;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;

class CpfBrokerReliabilityAutoConfigurationCompositionTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CpfBrokerReliabilityAutoConfiguration.class,
                    CpfMessagingRuntimeControlAutoConfiguration.class))
            .withPropertyValues("cpf.messaging.reliability.schema-required=false")
            .withBean(CpfExecutionIdGenerator.class, () -> mock(CpfExecutionIdGenerator.class))
            .withBean(CpfNamedBrokerClient.class, () -> new CpfNamedBrokerClient(
                    "test", "test", true, mock(CpfMessagingTemplate.class)))
            .withBean("cpfJdbcTemplate", JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            // Broker 신뢰성 원장은 CPF Platform DB Role 로 해석한다. Runtime 과 동일하게
            // Role Registry 를 제공해야 하며, raw DataSource 만으로는 계약을 만족하지 않는다.
            .withBean(CpfDataSourceRegistry.class, () -> role -> {
                if (role != CpfDatabaseRole.CPF_PLATFORM_DB) {
                    throw new IllegalStateException("unexpected role: " + role);
                }
                return mock(DataSource.class);
            });

    @Test
    void reliabilityAndRuntimeControlShareExactlyOnePolicyAndWorker() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CpfBrokerConsumerRuntimePolicy.class);
            assertThat(context).hasSingleBean(CpfBrokerConsumerWorker.class);
        });
    }

    @Test
    void applicationPolicyOverridesBothAutoConfigurations() {
        CpfBrokerConsumerRuntimePolicy applicationPolicy = new CpfBrokerConsumerRuntimePolicy();
        runner.withBean("applicationBrokerConsumerRuntimePolicy",
                        CpfBrokerConsumerRuntimePolicy.class, () -> applicationPolicy)
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CpfBrokerConsumerRuntimePolicy.class);
                    assertThat(context.getBean(CpfBrokerConsumerRuntimePolicy.class))
                            .isSameAs(applicationPolicy);
                    assertThat(context).hasSingleBean(CpfBrokerConsumerWorker.class);
                });
    }
}
