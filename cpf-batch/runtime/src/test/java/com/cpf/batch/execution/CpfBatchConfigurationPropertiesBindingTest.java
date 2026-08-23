package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class CpfBatchConfigurationPropertiesBindingTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BindingConfiguration.class)
            .withPropertyValues(
                    "cpf.batch.remote.kafka.request-topic=cpf.batch.contract.requests",
                    "cpf.batch.remote.kafka.role=WORKER",
                    "cpf.batch.execution.default-chunk-size=321");

    @Test
    void bindsCanonicalConstructorsWhenCompatibilityConstructorsAlsoExist() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            CpfBatchKafkaRemoteProperties remote = context.getBean(CpfBatchKafkaRemoteProperties.class);
            CpfBatchExecutionProperties execution = context.getBean(CpfBatchExecutionProperties.class);
            assertEquals("cpf.batch.contract.requests", remote.requestTopic());
            assertEquals(CpfBatchKafkaRemoteProperties.Role.WORKER, remote.role());
            assertEquals(321, execution.defaultChunkSize());
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties({CpfBatchKafkaRemoteProperties.class, CpfBatchExecutionProperties.class})
    static class BindingConfiguration {}
}
