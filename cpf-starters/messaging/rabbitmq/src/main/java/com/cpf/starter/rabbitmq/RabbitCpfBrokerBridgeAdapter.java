package com.cpf.starter.rabbitmq;

import com.cpf.core.api.broker.CpfBrokerBridgeHandler;
import com.cpf.core.api.broker.CpfBrokerBridgeMessage;
import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.core.api.broker.CpfBrokerBridgeResult;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.workflow.CpfWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import java.time.Clock;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent = new ConcurrentLinkedDeque<>();
    private final Map<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<String, SimpleMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public RabbitCpfBrokerBridgeAdapter(
            RabbitTemplate template,
            SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties,
            ObjectMapper mapper) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC());
    }

    RabbitCpfBrokerBridgeAdapter(
            RabbitTemplate template,
            SimpleRabbitListenerContainerFactory listenerFactory,
            CpfRabbitMqProperties properties,
            ObjectMapper mapper,
            Clock clock) {
        this.template = Objects.requireNonNull(template, "template");
        this.listenerFactory = Objects.requireNonNull(listenerFactory, "listenerFactory");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfBrokerBridgeResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> additionalHeaders) {
        String routingKey = required(destination, "destination");
        String resolvedKey = hasText(key) ? key.trim() : CpfTransactionContext.transactionId();
        Map<String, String> headers = propagationHeaders(additionalHeaders);
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
        MessageBuilder builder = MessageBuilder.withBody(body)
                .setMessageId(resolvedKey)
                .setContentType("application/json")
                .setHeader("cpf-bridge-destination", routingKey)
                .setHeader("cpf-transaction-id", headers.get(CpfHeaders.transactionId()));
        headers.forEach((name, value) -> builder.setHeader(name, value));
        Message providerMessage = builder.build();
        CorrelationData correlation = new CorrelationData(resolvedKey);
        try {
            template.send(properties.getExchange(), routingKey, providerMessage, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(
                    properties.getConfirmTimeout().toMillis(), TimeUnit.MILLISECONDS);
            if (!confirm.isAck() || correlation.getReturned() != null) {
                return new CpfBrokerBridgeResult(
                        false, "RABBITMQ", routingKey, resolvedKey,
                        headers.get(CpfHeaders.transactionId()),
                        confirm.isAck() ? "mandatory-return" : "broker-nack");
            }
            remember(bridgeMessage);
            return new CpfBrokerBridgeResult(
                    true, "RABBITMQ", routingKey, resolvedKey,
                    headers.get(CpfHeaders.transactionId()), "publisher-confirm=ACK");
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
            List<CpfBrokerBridgeHandler> targets = handlers.getOrDefault(
                    properties.getQueue(), new CopyOnWriteArrayList<>());
            if (targets.isEmpty()) {
                throw new IllegalStateException("No RabbitMQ bridge consumer is registered");
            }
            for (CpfBrokerBridgeHandler handler : targets) {
                handler.handle(message);
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
                .sorted(Comparator.comparing(CpfBrokerBridgeMessage::createdAt).reversed())
                .limit(bounded)
                .toList();
    }

    private Map<String, String> propagationHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(CpfTransactionContext.propagationHeaders());
        headers.putAll(CpfWorkflow.propagationHeaders());
        Set<String> reserved = headers.keySet().stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        if (additionalHeaders != null) {
            for (var entry : additionalHeaders.entrySet()) {
                String name = required(entry.getKey(), "header name");
                String value = Objects.requireNonNull(entry.getValue(), "header value");
                if (reserved.contains(name.toLowerCase(Locale.ROOT))) {
                    throw new SecurityException("CPF propagation header cannot be overridden: " + name);
                }
                headers.put(name, value);
            }
        }
        return Map.copyOf(headers);
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
    }
}
