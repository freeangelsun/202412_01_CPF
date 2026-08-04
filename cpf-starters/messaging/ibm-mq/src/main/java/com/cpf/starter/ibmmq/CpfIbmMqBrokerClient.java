package com.cpf.starter.ibmmq;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

/**
 * IBM MQ JMS Adapter that preserves reason codes without exposing IBM MQ SDK types.
 *
 * <p>CPF identity, idempotency and content-type properties are reserved. User headers are
 * normalized and validated before the provider call so they cannot overwrite CPF metadata or
 * collapse into the same IBM MQ/JMS property name.</p>
 */
public final class CpfIbmMqBrokerClient implements CpfBrokerClient {
    private static final Pattern REASON_CODE = Pattern.compile("\\b(2[0-9]{3})\\b");
    private static final Set<String> RESERVED_PROPERTY_NAMES = Set.of(
            "cpfmessageid", "cpfidempotencykey", "cpfcontenttype");

    private final JmsTemplate template;
    private final CpfIbmMqProperties properties;
    private final Clock clock;

    public CpfIbmMqBrokerClient(JmsTemplate template, CpfIbmMqProperties properties) {
        this(template, properties, Clock.systemUTC());
    }

    CpfIbmMqBrokerClient(
            JmsTemplate template, CpfIbmMqProperties properties, Clock clock) {
        this.template = java.util.Objects.requireNonNull(template, "template");
        this.properties = java.util.Objects.requireNonNull(properties, "properties");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        if (request.payload().length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("IBM MQ payload exceeds CPF maximum size");
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
                    "IBM_MQ",
                    properties.getDestination(),
                    Instant.now(clock),
                    "JMS transaction accepted");
        } catch (JmsException failure) {
            throw new UnknownResultException(reason(failure), failure);
        }
    }

    private static Map<String, String> normalizeUserProperties(Map<String, String> headers) {
        Map<String, String> normalized = new LinkedHashMap<>();
        for (var header : headers.entrySet()) {
            String propertyName = safeName(header.getKey());
            if (RESERVED_PROPERTY_NAMES.contains(propertyName.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        "IBM MQ header conflicts with reserved CPF property: " + header.getKey());
            }
            if (normalized.putIfAbsent(propertyName, header.getValue()) != null) {
                throw new IllegalArgumentException(
                        "IBM MQ headers normalize to the same property: " + propertyName);
            }
        }
        return Map.copyOf(normalized);
    }

    private static String safeName(String name) {
        String value = name.replaceAll("[^A-Za-z0-9_]", "_");
        if (value.isBlank() || Character.isDigit(value.charAt(0))) {
            value = "cpf_" + value;
        }
        return value;
    }

    private static String reason(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            String message = current.getMessage();
            if (message == null) {
                continue;
            }
            Matcher matcher = REASON_CODE.matcher(message);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return "UNMAPPED";
    }

    public static final class UnknownResultException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public UnknownResultException(String reasonCode, Throwable cause) {
            super("IBM MQ result is UNKNOWN; reason=" + reasonCode + "; reconcile before retry", cause);
        }
    }
}
