package com.cpf.messaging.jms;

import com.cpf.messaging.api.CpfBrokerBridgeHandler;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;

import com.cpf.messaging.context.*;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
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
    private final CpfMessageBridgeContextSupport contextSupport;
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recent = new ConcurrentLinkedDeque<>();
    private final Map<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers = new ConcurrentHashMap<>();
    private final Map<String, String> consumerGroups = new ConcurrentHashMap<>();
    private final Map<String, DefaultMessageListenerContainer> containers = new ConcurrentHashMap<>();

    public JmsCpfBrokerBridgeAdapter(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory listenerFactory,
            CpfJmsProperties properties,
            ObjectMapper mapper) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC(), defaultContextSupport());
    }

    public JmsCpfBrokerBridgeAdapter(
            JmsTemplate template, DefaultJmsListenerContainerFactory listenerFactory,
            CpfJmsProperties properties, ObjectMapper mapper, CpfMessageBridgeContextSupport contextSupport) {
        this(template, listenerFactory, properties, mapper, Clock.systemUTC(), contextSupport);
    }

    JmsCpfBrokerBridgeAdapter(
            JmsTemplate template,
            DefaultJmsListenerContainerFactory listenerFactory,
            CpfJmsProperties properties,
            ObjectMapper mapper,
            Clock clock) {
        this(template, listenerFactory, properties, mapper, clock, defaultContextSupport());
    }

    JmsCpfBrokerBridgeAdapter(
            JmsTemplate template, DefaultJmsListenerContainerFactory listenerFactory, CpfJmsProperties properties,
            ObjectMapper mapper, Clock clock, CpfMessageBridgeContextSupport contextSupport) {
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
        String resolvedDestination = required(destination, "destination");
        CpfMessageBridgeContextSupport.Outbound outbound = contextSupport.prepareOutbound("JMS", resolvedDestination, key, additionalHeaders);
        String resolvedKey = outbound.messageId();
        Map<String, String> headers = outbound.headers();
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
                message.setJMSCorrelationID(headers.get(CpfMessageHeaderNames.TRANSACTION_ID));
                message.setStringProperty("cpfBridgeDestination", resolvedDestination);
                message.setStringProperty("cpfBridgeKey", resolvedKey);
                writeContextProperties(message, headers);
                return message;
            });
            remember(bridgeMessage);
            return new CpfBrokerBridgeResult(
                    true,
                    "JMS",
                    resolvedDestination,
                    resolvedKey,
                    headers.get(CpfMessageHeaderNames.TRANSACTION_ID),
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
        String group = resolveGroup(resolvedDestination, handler.consumerGroup());
        String existingGroup = consumerGroups.putIfAbsent(resolvedDestination, group);
        if (existingGroup != null && !existingGroup.equals(group)) {
            throw new IllegalStateException("JMS destination already bound to different CPF consumerGroup: " + resolvedDestination);
        }
        handlers.computeIfAbsent(resolvedDestination, ignored -> new CopyOnWriteArrayList<>()).add(handler);
        containers.computeIfAbsent(resolvedDestination, this::startContainer);
    }

    private DefaultMessageListenerContainer startContainer(String destination) {
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("cpf-jms-" + Integer.toUnsignedString(destination.hashCode()));
        endpoint.setDestination(destination);
        endpoint.setMessageListener((jakarta.jms.MessageListener) this::consume);
        DefaultMessageListenerContainer container = listenerFactory.createListenerContainer(endpoint);
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
            Map<String,String> transportHeaders = readContextProperties(providerMessage);
            if (!Objects.equals(providerMessage.getJMSCorrelationID(), transportHeaders.get(CpfMessageHeaderNames.TRANSACTION_ID))) {
                throw new SecurityException("JMS correlation/context transaction mismatch");
            }
            int deliveryAttempt = providerMessage.propertyExists("JMSXDeliveryCount") ? Math.max(1, providerMessage.getIntProperty("JMSXDeliveryCount")) : 1;
            String group = consumerGroups.get(message.destination());
            if (!hasText(group)) throw new IllegalStateException("JMS CPF consumerGroup is not registered");
            var bundle = contextSupport.extractInbound("JMS", required(message.key(), "message key"), message.destination(), null, group, null, providerMessage.getJMSMessageID(), deliveryAttempt, providerMessage.getJMSRedelivered(), null, null, transportHeaders, null);
            remember(message);
            List<CpfBrokerBridgeHandler> targets = handlers.getOrDefault(
                    message.destination(), new CopyOnWriteArrayList<>());
            if (targets.isEmpty()) {
                throw new IllegalStateException("No JMS bridge consumer is registered for destination");
            }
            for (CpfBrokerBridgeHandler handler : targets) {
                contextSupport.consume(bundle, () -> handler.handle(message));
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

    private static void writeContextProperties(Message message, Map<String,String> headers) throws jakarta.jms.JMSException {
        for (var e : headers.entrySet()) message.setStringProperty(jmsProperty(e.getKey()), e.getValue());
    }

    private static Map<String,String> readContextProperties(Message message) throws Exception {
        Map<String,String> result=new LinkedHashMap<>();
        for(String name : CpfMessageHeaderNames.ALL) { String p=jmsProperty(name); if(message.propertyExists(p)) result.put(name,message.getStringProperty(p)); }
        return Map.copyOf(result);
    }

    private static String jmsProperty(String canonical) { return "cpfCtx_" + canonical.replace("cpf-", "").replace("-", "_"); }


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
