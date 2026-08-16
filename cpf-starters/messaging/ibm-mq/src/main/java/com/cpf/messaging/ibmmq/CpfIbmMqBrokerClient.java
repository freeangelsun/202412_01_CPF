package com.cpf.messaging.ibmmq;

import com.cpf.core.api.context.CpfContexts;

import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
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
            "cpfmessageid", "cpftransactionid", "cpfidempotencykey", "cpfcontenttype",
            "cpfsegmentid", "cpfproducermodule", "cpfconsumermodule",
            "jmscorrelationid", "jmsmessageid", "jmstimestamp", "jmsdestination",
            "jmsdeliverymode", "jmsredelivered", "jmstype", "jmsexpiration", "jmspriority");

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
        java.util.Objects.requireNonNull(request, "request");
        var current = CpfContexts.requireCurrent();
        if (!current.transaction().transactionId().equals(request.transactionId())) {
            throw new SecurityException("Provider request transactionId does not match bound CPF Context");
        }
        requireTracking(request.transactionId(), "transactionId");
        requireTracking(request.idempotencyKey(), "idempotencyKey");
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
        if (headers == null) {
            throw new IllegalArgumentException("IBM MQ headers must not be null");
        }
        Map<String, String> projectedNames = new LinkedHashMap<>();
        for (var header : headers.entrySet()) {
            String name = header.getKey();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("IBM MQ header name must not be blank");
            }
            if (!name.equals(name.trim())) {
                throw new IllegalArgumentException(
                        "IBM MQ header name must not contain surrounding whitespace: " + name);
            }
            if (header.getValue() == null) {
                throw new IllegalArgumentException("IBM MQ header value must not be null: " + name);
            }
            String propertyName = safeName(name);
            String collisionKey = propertyName.toLowerCase(Locale.ROOT);
            if (RESERVED_PROPERTY_NAMES.contains(collisionKey)) {
                throw new IllegalArgumentException(
                        "IBM MQ header conflicts with reserved CPF/JMS property: " + name);
            }
            String previous = projectedNames.putIfAbsent(collisionKey, name);
            if (previous != null) {
                throw new IllegalArgumentException(
                        "IBM MQ headers normalize to the same property: " + previous + " / " + name);
            }
            normalized.put(propertyName, header.getValue());
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
