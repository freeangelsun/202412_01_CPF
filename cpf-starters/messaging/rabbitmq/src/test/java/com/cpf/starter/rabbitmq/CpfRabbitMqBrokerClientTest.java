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

    @Test
    void enqueueMapsNackToFailedAndMasksReason() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(
                    false, "authorization=Bearer abc.def password=hunter2"));
            return null;
        }).when(template).send(eq("cpf.exchange"), eq("cpf.route"),
                any(Message.class), any(CorrelationData.class));
        var client = new CpfRabbitMqBrokerClient(template, properties());

        var result = client.enqueue(request(Map.of()));

        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.detail()).contains("***")
                .doesNotContain("abc.def", "hunter2");
    }

    @Test
    void headerValidationRejectsTrimmedCaseInsensitiveCollision() {
        Map<String, String> headers = new java.util.LinkedHashMap<>();
        headers.put("X-Source", "one");
        headers.put("x-source", "two");

        assertThatIllegalArgumentException()
                .isThrownBy(() -> CpfRabbitMqBrokerClient.validateUserHeaders(headers))
                .withMessageContaining("same name");
    }


    @Test
    void headerValidationRejectsSurroundingWhitespaceAndAllCpfReservedNames() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CpfRabbitMqBrokerClient.validateUserHeaders(
                        Map.of(" x-source", "value")))
                .withMessageContaining("surrounding whitespace");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> CpfRabbitMqBrokerClient.validateUserHeaders(
                        Map.of("CPF-MESSAGE-ID", "attack")))
                .withMessageContaining("reserved CPF header");
    }

    @Test
    void enqueueRejectsMissingTrackingBeforeProviderCall() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var client = new CpfRabbitMqBrokerClient(template, properties());
        CpfBrokerPublishRequest missingTransaction = new CpfBrokerPublishRequest(
                "M-X", "topic", "key", new byte[] {1}, "application/octet-stream",
                null, "segment", "producer", "consumer", "idem", Map.of(), Map.of());
        CpfBrokerPublishRequest missingIdempotency = new CpfBrokerPublishRequest(
                "M-X", "topic", "key", new byte[] {1}, "application/octet-stream",
                "tx", "segment", "producer", "consumer", null, Map.of(), Map.of());

        assertThatIllegalArgumentException().isThrownBy(() -> client.enqueue(missingTransaction));
        assertThatIllegalArgumentException().isThrownBy(() -> client.enqueue(missingIdempotency));
        verifyNoInteractions(template);
    }

    private static CpfRabbitMqProperties properties() {
        var properties = new CpfRabbitMqProperties();
        properties.setExchange("cpf.exchange");
        properties.setRoutingKey("cpf.route");
        properties.setMaxPayloadBytes(1024);
        return properties;
    }

    @Test
    void headerValidationReturnsImmutableSnapshot() {
        java.util.Map<String, String> source = new java.util.LinkedHashMap<>();
        source.put("x-source", "original");

        java.util.Map<String, String> snapshot = CpfRabbitMqBrokerClient.validateUserHeaders(source);
        source.put("x-source", "mutated");
        source.put("cpf-transaction-id", "attack");

        org.assertj.core.api.Assertions.assertThat(snapshot)
                .containsEntry("x-source", "original")
                .doesNotContainKey("cpf-transaction-id");
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> snapshot.put("x", "y"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void headerValidationRejectsBlankNameAndNullValue() {
        java.util.Map<String, String> blankName = new java.util.LinkedHashMap<>();
        blankName.put(" ", "value");
        java.util.Map<String, String> nullValue = new java.util.LinkedHashMap<>();
        nullValue.put("x-source", null);

        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> CpfRabbitMqBrokerClient.validateUserHeaders(blankName));
        org.assertj.core.api.Assertions.assertThatIllegalArgumentException()
                .isThrownBy(() -> CpfRabbitMqBrokerClient.validateUserHeaders(nullValue));
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
