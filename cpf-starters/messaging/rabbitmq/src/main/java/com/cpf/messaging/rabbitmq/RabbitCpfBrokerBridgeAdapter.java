package com.cpf.messaging.rabbitmq;

import com.cpf.messaging.api.CpfBrokerBridgeHandler;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;

import com.cpf.messaging.context.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.time.Clock;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;

/** RabbitMQ publisher-confirm + manual ACK/NACK bridge adapter. */
public final class RabbitCpfBrokerBridgeAdapter implements CpfBrokerBridgePort, AutoCloseable {
    private static final int RECENT_LIMIT = 200;
    private final RabbitTemplate template;
    private final SimpleRabbitListenerContainerFactory listenerFactory;
    private final CpfRabbitMqProperties properties;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final CpfMessageBridgeContextSupport contextSupport;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent = new ConcurrentLinkedDeque<>();
    private final Map<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<String, String> consumerGroups = new ConcurrentHashMap<>();
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public RabbitCpfBrokerBridgeAdapter(
            RabbitTemplate template,
            SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties,
            ObjectMapper mapper) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC(), defaultContextSupport());
    }

    public RabbitCpfBrokerBridgeAdapter(RabbitTemplate template, SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties, ObjectMapper mapper, CpfMessageBridgeContextSupport contextSupport) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC(), contextSupport);
    }

    RabbitCpfBrokerBridgeAdapter(
            RabbitTemplate template,
            SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties,
            ObjectMapper mapper,
            Clock clock) {
        this(template, listenerFactory, properties, mapper, clock, defaultContextSupport());
    }

    RabbitCpfBrokerBridgeAdapter(RabbitTemplate template, SimpleRabbitListenerContainerFactory listenerFactory, CpfRabbitMqProperties properties, ObjectMapper mapper, Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        this.template = Objects.requireNonNull(template, "template");
        this.listenerFactory = Objects.requireNonNull(listenerFactory, "listenerFactory");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.contextSupport = Objects.requireNonNull(contextSupport, "contextSupport");
    }

    @Override
    public CpfBrokerBridgeResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> additionalHeaders) {
        String routingKey = required(destination, "destination");
        CpfMessageBridgeContextSupport.Outbound outbound = contextSupport.prepareOutbound("RABBITMQ", routingKey, key, additionalHeaders);
        String resolvedKey = outbound.messageId(); Map<String,String> headers = outbound.headers();
        CpfBrokerBridgeMessage bridgeMessage = new CpfBrokerBridgeMessage(
                "RABBITMQ", routingKey, resolvedKey, payload, headers, clock.instant());
        byte[] body;
        try {
            body = mapper.writeValueAsBytes(bridgeMessage);
        } catch (Exception failure) {
            throw new IllegalArgumentException("RabbitMQ bridge payload cannot be serialized", failure);
        }
        if (body.length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("RabbitMQ bridge payload exceeds maximum size");
        }
        var builder = MessageBuilder.withBody(body)
                .setMessageId(resolvedKey)
                .setContentType("application/json")
                .setHeader("cpf-bridge-destination", routingKey)
                .setHeader("cpf-transaction-id", headers.get(CpfMessageHeaderNames.TRANSACTION_ID));
        headers.forEach((name, value) -> builder.setHeader(name, value));
        Message providerMessage = builder.build();
        CorrelationData correlation = new CorrelationData(resolvedKey);
        try {
            template.send(properties.getExchange(), routingKey, providerMessage, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(
                    properties.getConfirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                return new CpfBrokerBridgeResult(
                        false, "RABBITMQ", routingKey, resolvedKey,
                        headers.get(CpfMessageHeaderNames.TRANSACTION_ID),
                        confirm.ack() ? "mandatory-return" : "broker-nack");
            }
            remember(bridgeMessage);
            return new CpfBrokerBridgeResult(
                    true, "RABBITMQ", routingKey, resolvedKey,
                    headers.get(CpfMessageHeaderNames.TRANSACTION_ID), "publisher-confirm=ACK");
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw unknown("RabbitMQ bridge confirm wait was interrupted", interrupted);
        } catch (TimeoutException timeout) {
            throw unknown("RabbitMQ bridge confirm timed out", timeout);
        } catch (ExecutionException execution) {
            throw unknown("RabbitMQ bridge confirm failed", execution.getCause() == null ? execution : execution.getCause());
        } catch (RuntimeException failure) {
            throw unknown("RabbitMQ bridge publish result is unknown", failure);
        }
    }

    @Override
    public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
        String queue = required(destination, "destination");
        if (!queue.equals(properties.getQueue())) {
            throw new IllegalArgumentException(
                    "RabbitMQ bridge subscription must use configured queue: " + properties.getQueue());
        }
        Objects.requireNonNull(handler, "handler");
        String group = resolveGroup(queue, handler.consumerGroup());
        String existingGroup = consumerGroups.putIfAbsent(queue, group);
        if (existingGroup != null && !existingGroup.equals(group)) {
            throw new IllegalStateException("RabbitMQ queue already bound to different CPF consumerGroup: " + queue);
        }
        handlers.computeIfAbsent(queue, ignored -> new CopyOnWriteArrayList<>()).add(handler);
        containers.computeIfAbsent(queue, this::startContainer);
    }

    private SimpleMessageListenerContainer startContainer(String queue) {
        SimpleMessageListenerContainer container = listenerFactory.createListenerContainer();
        container.setQueueNames(queue);
        container.setMessageListener((ChannelAwareMessageListener) this::consume);
        container.afterPropertiesSet();
        container.start();
        return container;
    }

    private void consume(Message providerMessage, Channel channel) throws Exception {
        long deliveryTag = providerMessage.getMessageProperties().getDeliveryTag();
        try {
            byte[] body = providerMessage.getBody();
            if (body.length > properties.getMaxPayloadBytes()) {
                throw new IllegalArgumentException("RabbitMQ bridge consumer payload exceeds maximum size");
            }
            CpfBrokerBridgeMessage message = mapper.readValue(body, CpfBrokerBridgeMessage.class);
            Object providerDestination = providerMessage.getMessageProperties().getHeaders()
                    .get("cpf-bridge-destination");
            if (providerDestination != null && !providerDestination.toString().equals(message.destination())) {
                throw new IllegalArgumentException("RabbitMQ bridge destination metadata mismatch");
            }
            Map<String,String> transportHeaders = new LinkedHashMap<>();
            providerMessage.getMessageProperties().getHeaders().forEach((n,v) -> { if (v != null) transportHeaders.put(n, String.valueOf(v)); });
            boolean redelivered = Boolean.TRUE.equals(providerMessage.getMessageProperties().getRedelivered());
            int attempt = redelivered ? 2 : 1;
            String group = consumerGroups.get(message.destination());
            if (!hasText(group)) throw new IllegalStateException("RabbitMQ CPF consumerGroup is not registered");
            var bundle = contextSupport.extractInbound("RABBITMQ", required(message.key(), "message key"), message.destination(), null, group, null, Long.toString(deliveryTag), attempt, redelivered, null, null, transportHeaders, null);
            List<CpfBrokerBridgeHandler> targets = handlers.getOrDefault(
                    properties.getQueue(), new CopyOnWriteArrayList<>());
            if (targets.isEmpty()) {
                throw new IllegalStateException("No RabbitMQ bridge consumer is registered");
            }
            for (CpfBrokerBridgeHandler handler : targets) {
                contextSupport.consume(bundle, () -> handler.handle(message));
            }
            remember(message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception failure) {
            boolean redelivered = Boolean.TRUE.equals(providerMessage.getMessageProperties().getRedelivered());
            channel.basicNack(deliveryTag, false, !redelivered);
            throw failure;
        }
    }

    @Override
    public List<CpfBrokerBridgeMessage> findRecent(String destination, int limit) {
        String selected = hasText(destination) ? destination.trim() : null;
        int bounded = Math.max(1, Math.min(limit <= 0 ? 50 : limit, RECENT_LIMIT));
        return recent.stream()
                .filter(message -> selected == null || selected.equals(message.destination()))
                .sorted(Comparator.comparing(value -> value.createdAt()).reversed())
                .limit(bounded)
                .toList();
    }


    private static CpfMessageBridgeContextSupport defaultContextSupport() {
        CpfExecutionIdGenerator ex = new CpfExecutionIdGenerator() {
            public String newExecutionId() { return "EX-" + UUID.randomUUID(); }
            public String newSegmentId() { return "SG-" + UUID.randomUUID(); }
        };
        return new CpfMessageBridgeContextSupport(ex);
    }

    private void remember(CpfBrokerBridgeMessage message) {
        recent.addFirst(message);
        while (recent.size() > RECENT_LIMIT) {
            recent.pollLast();
        }
    }

    private static IllegalStateException unknown(String detail, Throwable cause) {
        return new IllegalStateException(detail + "; result is UNKNOWN and must be reconciled", cause);
    }

    private static String required(String value, String field) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static String resolveGroup(String destination, String requested) {
        return hasText(requested) ? requested.trim() : destination;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @Override
    public void close() {
        containers.values().forEach(container -> {
            try {
                container.stop();
            } finally {
                container.destroy();
            }
        });
        containers.clear();
        handlers.clear();
        consumerGroups.clear();
    }
}
