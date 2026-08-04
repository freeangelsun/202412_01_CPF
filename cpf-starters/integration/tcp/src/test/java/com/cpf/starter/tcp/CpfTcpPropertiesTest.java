package com.cpf.starter.tcp;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class CpfTcpPropertiesTest {
    @Test
    void rejectsInvalidUnknownResultLimit() {
        CpfTcpProperties properties = validProperties();
        properties.setMaxUnknownResults(0);
        IllegalStateException failure = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(failure.getMessage().contains("tracking limits"));
    }

    @Test
    void rejectsTimeoutThatCannotBeAppliedToSocket() {
        CpfTcpProperties properties = validProperties();
        properties.setResponseTimeout(Duration.ofMillis((long) Integer.MAX_VALUE + 1L));
        IllegalStateException failure = assertThrows(IllegalStateException.class, properties::validate);
        assertTrue(failure.getMessage().contains("response-timeout"));
    }

    private static CpfTcpProperties validProperties() {
        CpfTcpProperties properties = new CpfTcpProperties();
        properties.setEnabled(true);
        properties.setPort(12345);
        return properties;
    }
}
