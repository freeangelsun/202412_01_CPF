package com.cpf.starter.rabbitmq;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class CpfRabbitMqBrokerClientTest {

    @Test
    void enqueuePreservesContentTypeAndTrackingHeaders() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        AtomicReference<Message> captured = new AtomicReference<>();
        doAnswer(invocation -> {
            captured.set(invocation.getArgument(2));
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(template).send(eq("cpf.exchange"), eq("cpf.route"), any(Message.class), any(CorrelationData.class));
        var client = new CpfRabbitMqBrokerClient(template, properties());

        var result = client.enqueue(request(Map.of("x-cpf-source", "REF")));

        assertThat(result.status()).isEqualTo("PUBLISHED");
        assertThat(captured.get().getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(captured.get().getMessageProperties().getHeaders())
                .containsEntry("cpf-transaction-id", "T-AMQP-1")
                .containsEntry("cpf-idempotency-key", "ID-AMQP-1")
                .containsEntry("x-cpf-source", "REF");
    }

    @Test
    void enqueueRejectsReservedTrackingHeaderBeforeProviderCall() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var client = new CpfRabbitMqBrokerClient(template, properties());

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.enqueue(request(Map.of("CPF-TRANSACTION-ID", "ATTACK"))))
                .withMessageContaining("reserved CPF header");
        verifyNoInteractions(template);
    }

    private static CpfRabbitMqProperties properties() {
        var properties = new CpfRabbitMqProperties();
        properties.setExchange("cpf.exchange");
        properties.setRoutingKey("cpf.route");
        properties.setMaxPayloadBytes(1024);
        return properties;
    }

    private static CpfBrokerPublishRequest request(Map<String, String> headers) {
        return new CpfBrokerPublishRequest(
                "M-AMQP-1",
                "cpf.amqp",
                "K-AMQP-1",
                "{}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                "T-AMQP-1",
                "T-AMQP-1-RABBIT",
                "REF",
                "CMN",
                "ID-AMQP-1",
                headers,
                Map.of());
    }
}
