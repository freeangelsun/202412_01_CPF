package com.cpf.starter.jms;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import jakarta.jms.BytesMessage;
import jakarta.jms.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CpfJmsBrokerClientTest {
    private JmsTemplate template;
    private Session session;
    private BytesMessage message;
    private CpfJmsProperties properties;

    @BeforeEach
    void setUp() throws Exception {
        template = mock(JmsTemplate.class);
        session = mock(Session.class);
        message = mock(BytesMessage.class);
        properties = new CpfJmsProperties();
        properties.setDestination("CPF.QUEUE");
        properties.setMaxPayloadBytes(1024);
        when(session.createBytesMessage()).thenReturn(message);
        doAnswer(invocation -> {
            MessageCreator creator = invocation.getArgument(1);
            creator.createMessage(session);
            return null;
        }).when(template).send(eq("CPF.QUEUE"), any(MessageCreator.class));
    }

    @Test
    void enqueuePreservesContentTypeTrackingAndNormalizedHeaders() throws Exception {
        var client = new CpfJmsBrokerClient(template, properties);

        var result = client.enqueue(request(Map.of("x-cpf-source", "REF")));

        assertThat(result.status()).isEqualTo("PUBLISHED");
        verify(message).writeBytes("{}".getBytes(StandardCharsets.UTF_8));
        verify(message).setJMSCorrelationID("T-1");
        verify(message).setStringProperty("cpfMessageId", "M-1");
        verify(message).setStringProperty("cpfIdempotencyKey", "ID-1");
        verify(message).setStringProperty("cpfContentType", "application/json");
        verify(message).setStringProperty("x_cpf_source", "REF");
    }

    @Test
    void enqueueRejectsReservedMetadataOverrideBeforeProviderCall() {
        var client = new CpfJmsBrokerClient(template, properties);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.enqueue(request(Map.of("cpfMessageId", "ATTACK"))))
                .withMessageContaining("reserved CPF property");
        verifyNoInteractions(template);
    }

    @Test
    void enqueueRejectsHeadersThatNormalizeToSameJmsProperty() {
        var client = new CpfJmsBrokerClient(template, properties);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.enqueue(request(Map.of("x-cpf", "A", "x_cpf", "B"))))
                .withMessageContaining("normalize to the same property");
        verifyNoInteractions(template);
    }

    @Test
    void enqueueRejectsOversizedPayloadBeforeProviderCall() {
        properties.setMaxPayloadBytes(1);
        var client = new CpfJmsBrokerClient(template, properties);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.enqueue(request(Map.of())))
                .withMessageContaining("maximum size");
        verifyNoInteractions(template);
    }

    @Test
    void enqueueClassifiesTransportFailureAsUnknown() {
        JmsException failure = new JmsException("connection lost") { };
        doThrow(failure).when(template).send(eq("CPF.QUEUE"), any(MessageCreator.class));
        var client = new CpfJmsBrokerClient(template, properties);

        assertThatThrownBy(() -> client.enqueue(request(Map.of())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("UNKNOWN")
                .hasMessageContaining("reconcile")
                .hasCause(failure);
    }

    @Test
    void enqueueRejectsNullOrBlankHeaderBeforeProviderCall() {
        java.util.Map<String, String> blankName = new java.util.LinkedHashMap<>();
        blankName.put(" ", "value");
        java.util.Map<String, String> nullValue = new java.util.LinkedHashMap<>();
        nullValue.put("x-source", null);
        var client = new CpfJmsBrokerClient(template, properties);

        assertThatIllegalArgumentException().isThrownBy(() -> client.enqueue(request(blankName));
        assertThatIllegalArgumentException().isThrownBy(() -> client.enqueue(request(nullValue));
        verifyNoInteractions(template);
    }

    @Test
    void enqueueRejectsMissingTrackingBeforeProviderCall() {
        var client = new CpfJmsBrokerClient(template, properties);
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

    private static CpfBrokerPublishRequest request(Map<String, String> headers) {
        return new CpfBrokerPublishRequest(
                "M-1",
                "cpf.queue",
                "K-1",
                "{}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                "T-1",
                "T-1-JMS",
                "REF",
                "CMN",
                "ID-1",
                headers,
                Map.of("sampleId", "REF-MQ-001"));
    }
}
