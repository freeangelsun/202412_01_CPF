package com.cpf.reference.messaging.controller;

import com.cpf.core.api.broker.CpfBrokerBridgeHandler;
import com.cpf.core.api.broker.CpfBrokerBridgeMessage;
import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.core.api.broker.CpfBrokerBridgeResult;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ReferenceMessagingEducationControllerTest {

    @Test
    void enqueueEndpointCallsBrokerClientConsumerPath() {
        AtomicReference<CpfBrokerPublishRequest> captured = new AtomicReference<>();
        var controller = new ReferenceMessagingEducationController(
                new NoopBridgePort(),
                request -> {
                    captured.set(request);
                    return new CpfBrokerPublishResult(
                            "PUBLISHED",
                            request.messageId(),
                            "test-broker",
                            request.key(),
                            Instant.parse("2026-08-04T00:00:00Z"),
                            "accepted");
                });

        var response = controller.enqueueMessage("T-HTTP-1", "ID-HTTP-1");

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("PUBLISHED");
        assertThat(captured.get()).isNotNull();
        assertThat(captured.get().transactionId()).isEqualTo("T-HTTP-1");
        assertThat(captured.get().idempotencyKey()).isEqualTo("ID-HTTP-1");
    }

    private static final class NoopBridgePort implements CpfBrokerBridgePort {
        @Override
        public CpfBrokerBridgeResult publish(
                String destination,
                String key,
                Object payload,
                Map<String, String> headers) {
            return new CpfBrokerBridgeResult(true, "noop", destination, key, "T-NOOP", "noop");
        }

        @Override
        public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
            // No message is consumed in this endpoint-level test.
        }

        @Override
        public List<CpfBrokerBridgeMessage> findRecent(String destination, int limit) {
            return List.of();
        }
    }
}
