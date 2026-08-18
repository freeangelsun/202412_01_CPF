package com.cpf.messaging.rabbitmq;

import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
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
    private AutoCloseable cpfContextScope;
    @BeforeEach void bindCpfContext() {
        Clock clock=Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"),ZoneOffset.UTC);
        CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator() { private int n; public String newExecutionId(){return "EX-"+(++n);} public String newSegmentId(){return "S-AMQP-1";} };
        CpfContextExecutionFactory factory=new CpfContextExecutionFactory(() -> "T-AMQP-1",ids,() -> LocalDate.of(2026,8,18),clock);
        cpfContextScope=CpfContexts.bind(CpfContextSnapshot.capture(factory.newRoot(null,"messaging.test",null,null,clock.instant().plusSeconds(60)),clock.instant()));
    }
    @AfterEach void clearCpfContext() throws Exception { if(cpfContextScope!=null) cpfContextScope.close(); Thread.interrupted(); }


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

        var result = client.send(request(Map.of("x-cpf-source", "REF")));

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
                .isThrownBy(() -> client.send(request(Map.of("CPF-TRANSACTION-ID", "ATTACK"))))
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

        var result = client.send(request(Map.of()));

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
    void sendRejectsMissingIdempotencyBeforeProviderCall() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        var client = new CpfRabbitMqBrokerClient(template, properties());
        CpfBrokerPublishRequest missingIdempotency = new CpfBrokerPublishRequest(
                "M-X", "topic", "key", new byte[] {1}, "application/octet-stream", "producer", "consumer", null, Map.of(), Map.of());
        assertThatIllegalArgumentException().isThrownBy(() -> client.send(missingIdempotency));
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
                "REF",
                "CMN",
                "ID-AMQP-1",
                headers,
                Map.of());
    }
}
