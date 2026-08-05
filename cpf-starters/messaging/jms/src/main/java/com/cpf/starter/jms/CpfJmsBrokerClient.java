package com.cpf.starter.jms;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 * Provider-neutral JMS Adapter. A transport exception is always treated as UNKNOWN.
 *
 * <p>CPF tracking metadata is written through reserved JMS properties. User headers are normalized
 * and validated before the provider call so they cannot overwrite message identity, idempotency, or
 * content-type metadata. Different input names that normalize to the same JMS property are rejected
 * before any broker side effect.</p>
 */
public final class CpfJmsBrokerClient implements CpfBrokerClient {
    private static final Set<String> RESERVED_PROPERTY_NAMES = Set.of(
            "cpfmessageid", "cpfidempotencykey", "cpfcontenttype");

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
        requireTracking(request.transactionId(), "transactionId");
        requireTracking(request.idempotencyKey(), "idempotencyKey");
        if (request.payload().length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("JMS payload exceeds CPF maximum size");
        }
        Map<String, String> userProperties = normalizeUserProperties(request.headers());
        try {
            template.send(properties.getDestination(), session -> {
                var message = session.createBytesMessage();
                message.writeBytes(request.payload());
                message.setJMSCorrelationID(request.transactionId());
                message.setStringProperty("cpfMessageId", request.messageId());
                message.setStringProperty("cpfIdempotencyKey", request.idempotencyKey());
                message.setStringProperty("cpfContentType", request.contentType());
                for (var header : userProperties.entrySet()) {
                    message.setStringProperty(header.getKey(), header.getValue());
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

    private static Map<String, String> normalizeUserProperties(Map<String, String> headers) {
        Map<String, String> normalized = new LinkedHashMap<>();
        if (headers == null) {
            throw new IllegalArgumentException("JMS headers must not be null");
        }
        for (var header : headers.entrySet()) {
            if (header.getKey() == null || header.getKey().isBlank()) {
                throw new IllegalArgumentException("JMS header name must not be blank");
            }
            if (header.getValue() == null) {
                throw new IllegalArgumentException("JMS header value must not be null: " + header.getKey());
            }
            String propertyName = safeName(header.getKey().trim());
            if (RESERVED_PROPERTY_NAMES.contains(propertyName.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "JMS header conflicts with reserved CPF property: " + header.getKey());
            }
            if (normalized.putIfAbsent(propertyName, header.getValue()) != null) {
                throw new IllegalArgumentException(
                        "JMS headers normalize to the same property: " + propertyName);
            }
        }
        return Map.copyOf(normalized);
    }

    private static String requireTracking(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required before provider publish");
        }
        return value.trim();
    }

    private static String safeName(String name) {
        String value = name.replaceAll("[^A-Za-z0-9_]", "_");
        if (value.isBlank() || Character.isDigit(value.charAt(0))) {
            value = "cpf_" + value;
        }
        return value;
    }
}
