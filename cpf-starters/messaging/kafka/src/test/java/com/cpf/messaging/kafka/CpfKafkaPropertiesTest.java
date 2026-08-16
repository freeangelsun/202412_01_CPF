package com.cpf.messaging.kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class CpfKafkaPropertiesTest {
    @Test
    void defaultsDoNotClaimTheGlobalDefaultBinding() {
        var properties = new CpfKafkaProperties(Duration.ofSeconds(2), 1024, true);
        assertEquals("kafka", properties.bindingName());
        assertFalse(properties.defaultBinding());
    }

    @Test
    void blankBindingNameFailsClosed() {
        assertThrows(IllegalArgumentException.class,
                () -> new CpfKafkaProperties(Duration.ofSeconds(2), 1024, true, " ", false));
    }
}
