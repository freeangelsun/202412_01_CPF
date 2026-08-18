package com.cpf.messaging.ibmmq;

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

class CpfIbmMqBrokerClientTest {
    private AutoCloseable cpfContextScope;
    @BeforeEach void bindCpfContext() {
        Clock clock=Clock.fixed(Instant.parse("2026-08-18T00:00:00Z"),ZoneOffset.UTC);
        CpfExecutionIdGenerator ids=new CpfExecutionIdGenerator() { private int n; public String newExecutionId(){return "EX-"+(++n);} public String newSegmentId(){return "S-IBM-1";} };
        CpfContextExecutionFactory factory=new CpfContextExecutionFactory(() -> "T-IBM-1",ids,() -> LocalDate.of(2026,8,18),clock);
        cpfContextScope=CpfContexts.bind(CpfContextSnapshot.capture(factory.newRoot(null,"messaging.test",null,null,clock.instant().plusSeconds(60)),clock.instant()));
    }
    @AfterEach void clearCpfContext() throws Exception { if(cpfContextScope!=null) cpfContextScope.close(); Thread.interrupted(); }

    private JmsTemplate template;
    private Session session;
    private BytesMessage message;
    private CpfIbmMqProperties properties;

    @BeforeEach
    void setUp() throws Exception {
        template = mock(JmsTemplate.class);
        session = mock(Session.class);
        message = mock(BytesMessage.class);
        properties = new CpfIbmMqProperties();
        properties.setDestination("CPF.IBM.QUEUE");
        properties.setMaxPayloadBytes(1024);
        when(session.createBytesMessage()).thenReturn(message);
        doAnswer(invocation -> {
            MessageCreator creator = invocation.getArgument(1);
            creator.createMessage(session);
            return null;
        }).when(template).send(eq("CPF.IBM.QUEUE"), any(MessageCreator.class));
    }

    @Test
    void enqueuePreservesContentTypeTrackingAndUserHeaders() throws Exception {
        var client = new CpfIbmMqBrokerClient(template, properties);

        var result = client.send(request(Map.of("x-cpf-source", "REF")));

        assertThat(result.status()).isEqualTo("PUBLISHED");
        verify(message).writeBytes("{}".getBytes(StandardCharsets.UTF_8));
        verify(message).setJMSCorrelationID("T-IBM-1");
        verify(message).setStringProperty("cpfMessageId", "M-IBM-1");
        verify(message).setStringProperty("cpfIdempotencyKey", "ID-IBM-1");
        verify(message).setStringProperty("cpfContentType", "application/json");
        verify(message).setStringProperty("x_cpf_source", "REF");
    }

    @Test
    void enqueueRejectsReservedMetadataOverrideBeforeProviderCall() {
        var client = new CpfIbmMqBrokerClient(template, properties);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.send(request(Map.of("cpfIdempotencyKey", "ATTACK"))))
                .withMessageContaining("reserved CPF/JMS property");
        verifyNoInteractions(template);
    }

    @Test
    void enqueueRejectsNormalizedHeaderCollision() {
        var client = new CpfIbmMqBrokerClient(template, properties);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.send(request(Map.of("x-cpf", "A", "x_cpf", "B"))))
                .withMessageContaining("normalize to the same property");
        verifyNoInteractions(template);
    }


    @Test
    void enqueueRejectsJmsReservedNameWhitespaceAndCaseInsensitiveProjectionBeforeProviderCall() {
        var client = new CpfIbmMqBrokerClient(template, properties);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.send(request(Map.of("JMSCorrelationID", "ATTACK"))))
                .withMessageContaining("reserved CPF/JMS property");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.send(request(Map.of(" x-source", "value"))))
                .withMessageContaining("surrounding whitespace");
        java.util.Map<String, String> collision = new java.util.LinkedHashMap<>();
        collision.put("X-Cpf", "A");
        collision.put("x_cpf", "B");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> client.send(request(collision)))
                .withMessageContaining("same property");
        verifyNoInteractions(template);
    }

    @Test
    void unknownFailurePreservesIbmReasonCode() {
        JmsException failure = new JmsException("MQRC_CONNECTION_BROKEN 2009") { };
        doThrow(failure).when(template).send(eq("CPF.IBM.QUEUE"), any(MessageCreator.class));
        var client = new CpfIbmMqBrokerClient(template, properties);

        assertThatThrownBy(() -> client.send(request(Map.of())))
                .isInstanceOf(CpfIbmMqBrokerClient.UnknownResultException.class)
                .hasMessageContaining("reason=2009")
                .hasMessageContaining("reconcile")
                .hasCause(failure);
    }

    @Test
    void enqueueRejectsNullOrBlankHeaderBeforeProviderCall() {
        java.util.Map<String, String> blankName = new java.util.LinkedHashMap<>();
        blankName.put(" ", "value");
        java.util.Map<String, String> nullValue = new java.util.LinkedHashMap<>();
        nullValue.put("x-source", null);
        var client = new CpfIbmMqBrokerClient(template, properties);

        assertThatIllegalArgumentException().isThrownBy(() -> client.send(request(blankName)));
        assertThatIllegalArgumentException().isThrownBy(() -> client.send(request(nullValue)));
        verifyNoInteractions(template);
    }

    @Test
    void sendRejectsMissingIdempotencyBeforeProviderCall() {
        var client = new CpfIbmMqBrokerClient(template, properties);
        CpfBrokerPublishRequest missingIdempotency = new CpfBrokerPublishRequest(
                "M-X", "topic", "key", new byte[] {1}, "application/octet-stream", "producer", "consumer", null, Map.of(), Map.of());
        assertThatIllegalArgumentException().isThrownBy(() -> client.send(missingIdempotency));
        verifyNoInteractions(template);
    }

    private static CpfBrokerPublishRequest request(Map<String, String> headers) {
        return new CpfBrokerPublishRequest(
                "M-IBM-1",
                "cpf.ibm.queue",
                "K-IBM-1",
                "{}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                "REF",
                "CMN",
                "ID-IBM-1",
                headers,
                Map.of());
    }
}
