package com.cpf.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.cpf.starter.runtime.CpfCapabilityBindingCardinality;
import com.cpf.starter.runtime.CpfCapabilityBindingRegistry;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class CpfKafkaBindingAutoConfigurationTest {
    @Test
    void namedKafkaBindingDoesNotForceAGlobalDefaultWhenCapabilityIsOptional() {
        CpfCapabilityBindingRegistry registry = new CpfCapabilityBindingRegistry();
        CpfKafkaProperties properties = new CpfKafkaProperties(
                Duration.ofSeconds(2), 1024, true, "kafka", false);

        new CpfKafkaBindingAutoConfiguration().cpfKafkaNamedBrokerClient(
                mock(KafkaCpfMessagingTemplate.class), properties, registry);

        assertEquals(
                CpfCapabilityBindingCardinality.NAMED_MULTI_OPTIONAL_DEFAULT,
                registry.cardinality("messaging"));
        assertEquals(1, registry.list("messaging").size());
        assertDoesNotThrow(registry::validateAll);
        assertThrows(IllegalStateException.class, () -> registry.requireDefault("messaging"));
    }
}
