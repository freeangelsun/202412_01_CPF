package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class CpfBatchKafkaPropertiesBindingTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BindingConfiguration.class)
            .withPropertyValues(
                    "cpf.batch.remote.kafka.request-topic=cpf.batch.contract.requests",
                    "cpf.batch.remote.kafka.role=WORKER");

    @Test
    void bindsCanonicalKafkaAdapterProperties() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            CpfBatchKafkaRemoteProperties remote = context.getBean(CpfBatchKafkaRemoteProperties.class);
            assertEquals("cpf.batch.contract.requests", remote.requestTopic());
            assertEquals(CpfBatchKafkaRemoteProperties.Role.WORKER, remote.role());
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CpfBatchKafkaRemoteProperties.class)
    static class BindingConfiguration { }
}
