package com.cpf.starter.jms;

import com.cpf.core.api.broker.CpfBrokerBridgeHandler;
import com.cpf.core.api.broker.CpfBrokerBridgeMessage;
import com.cpf.core.api.broker.CpfBrokerBridgePort;
import com.cpf.core.api.broker.CpfBrokerBridgeResult;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.util.CpfHeaders;
import com.cpf.core.api.workflow.CpfWorkflow;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
import java.nio.charset.StandardCharsets;
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
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

/** JMS/IBM MQ 공통 publish/subscribe Product Adapter. */
public final class JmsCpfBrokerBridgeAdapter implements CpfBrokerBridgePort, AutoCloseable {
    private static final int RECENT_LIMIT = 200;
    private final JmsTemplate template;
    private final DefaultJmsListenerContainerFactory listenerFactory;
    private final CpfJmsProperties properties;
    private final ObjectMapper mapper;
    private final Clock clock;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent = new ConcurrentLinkedDeque<>();
    private final Map<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<String, DefaultMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public JmsCpfBrokerBridgeAdapter(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory listenerFactory,
            CpfJmsProperties properties,
            ObjectMapper mapper) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC());
    }

    JmsCpfBrokerBridgeAdapter(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory listenerFactory,
            CpfJmsProperties properties,
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
        String resolvedDestination = required(destination, "destination");
        String resolvedKey = hasText(key) ? key.trim() : CpfTransactionContext.transactionId();
        Map<String, String> headers = propagationHeaders(additionalHeaders);
        CpfBrokerBridgeMessage bridgeMessage = new CpfBrokerBridgeMessage(
                "JMS", resolvedDestination, resolvedKey, payload, headers, clock.instant());
        byte[] body;
        try {
            body = mapper.writeValueAsBytes(bridgeMessage);
        } catch (Exception failure) {
            throw new IllegalArgumentException("JMS bridge payload cannot be serialized", failure);
        }
        if (body.length > properties.getMaxPayloadBytes()) {
            throw new IllegalArgumentException("JMS bridge payload exceeds maximum size");
        }
        try {
            template.send(resolvedDestination, session -> {
                BytesMessage message = session.createBytesMessage();
                message.writeBytes(body);
                message.setJMSCorrelationID(headers.get(CpfHeaders.transactionId()));
                message.setStringProperty("cpfBridgeDestination", resolvedDestination);
                message.setStringProperty("cpfBridgeKey", resolvedKey);
                return message;
            });
            remember(bridgeMessage);
            return new CpfBrokerBridgeResult(
                    true,
                    "JMS",
                    resolvedDestination,
                    resolvedKey,
                    headers.get(CpfHeaders.transactionId()),
                    properties.isSessionTransacted() ? "session=transacted" : "session=acknowledged");
        } catch (RuntimeException failure) {
            throw new IllegalStateException(
                    "JMS bridge publish result is UNKNOWN; reconcile before retrying", failure);
        }
    }

    @Override
    public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
        String resolvedDestination = required(destination, "destination");
        Objects.requireNonNull(handler, "handler");
        handlers.computeIfAbsent(resolvedDestination, ignored -> new CopyOnWriteArrayList<>()).add(handler);
        containers.computeIfAbsent(resolvedDestination, this::startContainer);
    }

    private DefaultMessageListenerContainer startContainer(String destination) {
        DefaultMessageListenerContainer container = listenerFactory.createListenerContainer();
        container.setDestinationName(destination);
        container.setMessageListener((jakarta.jms.MessageListener) this::consume);
        container.afterPropertiesSet();
        container.start();
        return container;
    }

    private void consume(Message providerMessage) {
        try {
            byte[] body = messageBody(providerMessage);
            if (body.length > properties.getMaxPayloadBytes()) {
                throw new IllegalArgumentException("JMS bridge consumer payload exceeds maximum size");
            }
            CpfBrokerBridgeMessage message = mapper.readValue(body, CpfBrokerBridgeMessage.class);
            String providerDestination = providerMessage.getStringProperty("cpfBridgeDestination");
            if (hasText(providerDestination) && !providerDestination.equals(message.destination())) {
                throw new IllegalArgumentException("JMS bridge destination metadata mismatch");
            }
            remember(message);
            List<CpfBrokerBridgeHandler> targets = handlers.getOrDefault(
                    message.destination(), new CopyOnWriteArrayList<>());
            if (targets.isEmpty()) {
                throw new IllegalStateException("No JMS bridge consumer is registered for destination");
            }
            for (CpfBrokerBridgeHandler handler : targets) {
                handler.handle(message);
            }
            providerMessage.acknowledge();
        } catch (Exception failure) {
            throw new IllegalStateException("JMS bridge consumer rejected message for redelivery/DLQ", failure);
        }
    }

    private static byte[] messageBody(Message message) throws Exception {
        if (message instanceof BytesMessage bytes) {
            long length = bytes.getBodyLength();
            if (length < 0 || length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("JMS BytesMessage body length is invalid");
            }
            byte[] body = new byte[(int) length];
            int offset = 0;
            while (offset < body.length) {
                int read = bytes.readBytes(body, body.length - offset);
                if (read < 0) {
                    break;
                }
                offset += read;
            }
            if (offset != body.length) {
                throw new IllegalArgumentException("JMS BytesMessage body was truncated");
            }
            return body;
        }
        if (message instanceof TextMessage text) {
            return text.getText().getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalArgumentException("Unsupported JMS bridge message type: " + message.getClass().getName());
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
