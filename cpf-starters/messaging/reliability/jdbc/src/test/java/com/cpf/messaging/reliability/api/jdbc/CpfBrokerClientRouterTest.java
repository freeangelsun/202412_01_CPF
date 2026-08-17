package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.spi.CpfNamedBrokerClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class CpfMessagingTemplateRouterTest {
    private static final CpfMessagingTemplate CLIENT = new CpfMessagingTemplate() {
        @Override
        public CpfBrokerPublishResult send(CpfBrokerPublishRequest request) {
            return null;
        }
    };

    @Test
    void zeroProviderFailsClosed() {
        assertThrows(IllegalStateException.class, () -> new CpfMessagingTemplateRouter(List.of()));
    }

    @Test
    void zeroDefaultFailsClosedEvenForOneProvider() {
        assertThrows(IllegalStateException.class, () -> new CpfMessagingTemplateRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", false, CLIENT))));
    }

    @Test
    void twoDefaultsFailClosed() {
        assertThrows(IllegalStateException.class, () -> new CpfMessagingTemplateRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", true, CLIENT),
                new CpfNamedBrokerClient("rabbit", "rabbitmq", true, CLIENT))));
    }

    @Test
    void exactlyOneDefaultPasses() {
        assertDoesNotThrow(() -> new CpfMessagingTemplateRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", true, CLIENT),
                new CpfNamedBrokerClient("rabbit", "rabbitmq", false, CLIENT))));
    }
}
