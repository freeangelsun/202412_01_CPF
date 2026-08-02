package com.cpf.starter.rabbitmq;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

/** RabbitMQ confirms are mapped to explicit PUBLISHED, FAILED, or UNKNOWN outcomes. */
public final class CpfRabbitMqBrokerClient implements CpfBrokerClient {
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
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        if (request.payload().length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("RabbitMQ payload exceeds CPF maximum size");
        }
        MessageBuilder builder = MessageBuilder.withBody(request.payload())
                .setMessageId(request.messageId())
                .setContentType("application/octet-stream")
                .setHeader("cpf-transaction-id", request.transactionId())
                .setHeader("cpf-idempotency-key", request.idempotencyKey());
        request.headers().forEach(builder::setHeader);
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
        return detail == null || detail.isBlank() ? "unspecified" : detail;
    }
}
