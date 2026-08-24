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
            .withPropertyValues("cpf.batch.execution.default-chunk-size=321");

    @Test
    void bindsCanonicalConstructorsWhenCompatibilityConstructorsAlsoExist() {
        contextRunner.run(context -> {
            assertNull(context.getStartupFailure());
            CpfBatchExecutionProperties execution = context.getBean(CpfBatchExecutionProperties.class);
            assertEquals(321, execution.defaultChunkSize());
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(CpfBatchExecutionProperties.class)
    static class BindingConfiguration {}
}
