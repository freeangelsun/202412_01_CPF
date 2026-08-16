package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.spi.CpfNamedBrokerClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class CpfBrokerClientRouterTest {
    private static final CpfBrokerClient CLIENT = new CpfBrokerClient() {
        @Override
        public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
            return null;
        }
    };

    @Test
    void zeroProviderFailsClosed() {
        assertThrows(IllegalStateException.class, () -> new CpfBrokerClientRouter(List.of()));
    }

    @Test
    void zeroDefaultFailsClosedEvenForOneProvider() {
        assertThrows(IllegalStateException.class, () -> new CpfBrokerClientRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", false, CLIENT))));
    }

    @Test
    void twoDefaultsFailClosed() {
        assertThrows(IllegalStateException.class, () -> new CpfBrokerClientRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", true, CLIENT),
                new CpfNamedBrokerClient("rabbit", "rabbitmq", true, CLIENT))));
    }

    @Test
    void exactlyOneDefaultPasses() {
        assertDoesNotThrow(() -> new CpfBrokerClientRouter(List.of(
                new CpfNamedBrokerClient("kafka", "kafka", true, CLIENT),
                new CpfNamedBrokerClient("rabbit", "rabbitmq", false, CLIENT))));
    }
}
