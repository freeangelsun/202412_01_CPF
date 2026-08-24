package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.mock.env.MockEnvironment;

class CpfBatchKafkaWorkerConcurrencyTest {
    @Test
    void configuredCapacityIsAppliedBeforeTheKafkaWorkerContainerIsCreated() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.batch.worker.max-concurrency", "6");
        BeanPostProcessor customizer =
                CpfBatchKafkaRemoteConfiguration.cpfBatchWorkerKafkaConcurrencyCustomizer(environment);
        @SuppressWarnings("unchecked")
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                mock(ConcurrentKafkaListenerContainerFactory.class);

        Object result = customizer.postProcessBeforeInitialization(
                factory, "cpfBatchKafkaManualAckContainerFactory");

        assertThat(result).isSameAs(factory);
        verify(factory).setConcurrency(6);
    }

    @Test
    void invalidConfiguredCapacityFailsBeforeListenerStartup() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.batch.worker.max-concurrency", "0");

        assertThatThrownBy(() ->
                CpfBatchKafkaRemoteConfiguration.cpfBatchWorkerKafkaConcurrencyCustomizer(environment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("out of range");
    }
}
