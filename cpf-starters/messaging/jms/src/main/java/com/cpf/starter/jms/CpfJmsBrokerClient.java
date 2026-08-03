package com.cpf.starter.jms;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.time.Clock;
import java.time.Instant;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/** Provider-neutral JMS Adapter. A transport exception is always treated as UNKNOWN. */
public final class CpfJmsBrokerClient implements CpfBrokerClient {
    private final JmsTemplate template;
    private final CpfJmsProperties properties;
    private final Clock clock;

    public CpfJmsBrokerClient(JmsTemplate template, CpfJmsProperties properties) {
        this(template, properties, Clock.systemUTC());
    }

    CpfJmsBrokerClient(
            JmsTemplate template, CpfJmsProperties properties, Clock clock) {
        this.template = java.util.Objects.requireNonNull(template, "template");
        this.properties = java.util.Objects.requireNonNull(properties, "properties");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        if (request.payload().length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("JMS payload exceeds CPF maximum size");
        }
        try {
            template.send(properties.getDestination(), session -> {
                var message = session.createBytesMessage();
                message.writeBytes(request.payload());
                message.setJMSCorrelationID(request.transactionId());
                message.setStringProperty("cpfMessageId", request.messageId());
                message.setStringProperty("cpfIdempotencyKey", request.idempotencyKey());
                for (var header : request.headers().entrySet()) {
                    message.setStringProperty(safeName(header.getKey()), header.getValue());
                }
                return message;
            });
            return new CpfBrokerPublishResult(
                    "PUBLISHED",
                    request.messageId(),
                    "JMS",
                    properties.getDestination(),
                    Instant.now(clock),
                    properties.isSessionTransacted()
                            ? "session=transacted"
                            : "session=acknowledged");
        } catch (JmsException failure) {
            throw new IllegalStateException(
                    "JMS publish result is UNKNOWN; reconcile before retrying", failure);
        }
    }

    private static String safeName(String name) {
        String value = name.replaceAll("[^A-Za-z0-9_]", "_");
        if (value.isBlank() || Character.isDigit(value.charAt(0))) {
            value = "cpf_" + value;
        }
        return value;
    }
}
