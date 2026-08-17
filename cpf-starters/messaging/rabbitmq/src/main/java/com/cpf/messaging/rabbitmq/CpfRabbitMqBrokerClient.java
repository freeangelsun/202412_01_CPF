package com.cpf.messaging.rabbitmq;

import com.cpf.core.api.context.CpfContexts;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.spi.broker.CpfBrokerFailureSanitizer;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/**
 * RabbitMQ confirms are mapped to explicit PUBLISHED, FAILED, or UNKNOWN outcomes.
 *
 * <p>CPF transaction and idempotency headers are reserved and validated before publish. The
 * request content type is preserved instead of being replaced with a generic octet-stream value.</p>
 */
public final class CpfRabbitMqBrokerClient implements CpfMessagingTemplate {
    private static final Set<String> RESERVED_HEADERS = Set.of(
            "cpf-message-id", "cpf-transaction-id", "cpf-idempotency-key",
            "cpf-content-type", "cpf-segment-id", "cpf-producer-module",
            "cpf-consumer-module");

    private final RabbitTemplate template;
    private final CpfRabbitMqProperties properties;
    private final Clock clock;

    public CpfRabbitMqBrokerClient(
            RabbitTemplate template, CpfRabbitMqProperties properties) {
        this(template, properties, Clock.systemUTC());
    }

    CpfRabbitMqBrokerClient(
            RabbitTemplate template, CpfRabbitMqProperties properties, Clock clock) {
        this.template = java.util.Objects.requireNonNull(template, "template");
        this.properties = java.util.Objects.requireNonNull(properties, "properties");
        this.clock = java.util.Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfBrokerPublishResult send(CpfBrokerPublishRequest request) {
        java.util.Objects.requireNonNull(request, "request");
        var current = CpfContexts.requireCurrent();
        if (!current.transaction().transactionId().equals(request.transactionId())) {
            throw new SecurityException("Provider request transactionId does not match bound CPF Context");
        }
        requireTracking(request.transactionId(), "transactionId");
        requireTracking(request.idempotencyKey(), "idempotencyKey");
        if (request.payload().length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("RabbitMQ payload exceeds CPF maximum size");
        }
        Map<String, String> userHeaders = validateUserHeaders(request.headers());
        var builder = MessageBuilder.withBody(request.payload())
                .setMessageId(request.messageId())
                .setContentType(request.contentType())
                .setHeader("cpf-transaction-id", request.transactionId())
                .setHeader("cpf-idempotency-key", request.idempotencyKey());
        userHeaders.forEach(builder::setHeader);
        Message message = builder.build();
        CorrelationData correlation = new CorrelationData(request.messageId());
        try {
            template.send(
                    properties.getExchange(), properties.getRoutingKey(), message, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(
                    properties.getConfirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.isAck()) {
                return result(request, "FAILED", "broker-nack:" + safe(confirm.getReason()));
            }
            if (correlation.getReturned() != null) {
                return result(
                        request,
                        "FAILED",
                        "mandatory-return:" + safe(correlation.getReturned().getReplyText()));
            }
            return result(request, "PUBLISHED", "confirm=ACK");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw unknown("RabbitMQ confirm wait was interrupted", interrupted);
        } catch (TimeoutException timeout) {
            throw unknown("RabbitMQ confirm timed out", timeout);
        } catch (ExecutionException execution) {
            Throwable cause = execution.getCause() == null ? execution : execution.getCause();
            throw unknown("RabbitMQ confirm failed before a definitive result", cause);
        } catch (RuntimeException failure) {
            throw unknown("RabbitMQ publish result is unknown", failure);
        }
    }

    static Map<String, String> validateUserHeaders(Map<String, String> headers) {
        if (headers == null) {
            throw new IllegalArgumentException("RabbitMQ headers must not be null");
        }
        Map<String, String> snapshot = new LinkedHashMap<>();
        java.util.Set<String> normalizedNames = new java.util.HashSet<>();
        for (var header : headers.entrySet()) {
            String name = header.getKey();
            String value = header.getValue();
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("RabbitMQ header name must not be blank");
            }
            if (value == null) {
                throw new IllegalArgumentException("RabbitMQ header value must not be null: " + name);
            }
            if (!name.equals(name.trim())) {
                throw new IllegalArgumentException(
                        "RabbitMQ header name must not contain surrounding whitespace: " + name);
            }
            String normalizedName = name;
            String collisionKey = normalizedName.toLowerCase(Locale.ROOT);
            if (RESERVED_HEADERS.contains(collisionKey)) {
                throw new IllegalArgumentException(
                        "RabbitMQ header conflicts with reserved CPF header: " + name);
            }
            if (!normalizedNames.add(collisionKey)) {
                throw new IllegalArgumentException(
                        "RabbitMQ headers normalize to the same name: " + normalizedName);
            }
            snapshot.put(normalizedName, value);
        }
        return Map.copyOf(snapshot);
    }

    private static String requireTracking(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required before provider publish");
        }
        return value.trim();
    }

    private CpfBrokerPublishResult result(
            CpfBrokerPublishRequest request, String status, String detail) {
        return new CpfBrokerPublishResult(
                status,
                request.messageId(),
                "RABBITMQ",
                properties.getRoutingKey(),
                Instant.now(clock),
                detail);
    }

    private static IllegalStateException unknown(String message, Throwable cause) {
        return new IllegalStateException(
                message + "; reconcile before retrying the message", cause);
    }

    private static String safe(String detail) {
        return CpfBrokerFailureSanitizer.sanitize(detail == null || detail.isBlank() ? "unspecified" : detail);
    }
}
