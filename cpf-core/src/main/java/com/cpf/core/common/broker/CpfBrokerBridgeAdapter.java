package com.cpf.core.common.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.core.common.exception.CpfExternalServiceException;
import com.cpf.core.common.logging.TransactionContext;
import com.cpf.core.common.workflow.CpfWorkflowContext;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 인메모리, Kafka, RabbitMQ 발행을 CPF port 뒤에 통합하는 기본 adapter입니다.
 *
 * <p>실제 원격 소비는 각 broker listener가 동일한 handler 계약으로 전달합니다. 로컬 profile은
 * 외부 설치 없이 같은 프로세스의 구독자에게 결정적으로 전달합니다.</p>
 */
@Component
public class CpfBrokerBridgeAdapter implements CpfBrokerBridgePort {
    private static final int DEFAULT_RECENT_LIMIT = 200;

    private final Environment environment;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final ConcurrentMap<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers =
            new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recentMessages = new ConcurrentLinkedDeque<>();

    public CpfBrokerBridgeAdapter(
            Environment environment,
            ObjectMapper objectMapper,
            ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
            ObjectProvider<RabbitTemplate> rabbitTemplateProvider) {
        this.environment = environment;
        this.objectMapper = objectMapper;
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.rabbitTemplateProvider = rabbitTemplateProvider;
    }

    @Override
    public CpfBrokerBridgeResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> additionalHeaders) {
        String resolvedDestination = requiredText(destination, "destination");
        String resolvedKey = hasText(key) ? key : TransactionContext.getOrCreateTransactionId();
        String broker = brokerType();
        Map<String, String> headers = propagationHeaders(additionalHeaders);
        CpfBrokerBridgeMessage message = new CpfBrokerBridgeMessage(
                broker,
                resolvedDestination,
                resolvedKey,
                payload,
                headers,
                Instant.now());

        if (!enabled()) {
            return new CpfBrokerBridgeResult(
                    false,
                    broker,
                    resolvedDestination,
                    resolvedKey,
                    TransactionContext.getOrCreateTransactionId(),
                    "broker bridge가 설정으로 비활성화되어 있습니다.");
        }

        publishToAdapter(message);
        remember(message);
        dispatchLocal(message);
        return new CpfBrokerBridgeResult(
                true,
                broker,
                resolvedDestination,
                resolvedKey,
                headers.get(TransactionContext.HEADER_TRANSACTION_ID),
                "broker bridge 발행이 접수됐습니다.");
    }

    @Override
    public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
        String resolvedDestination = requiredText(destination, "destination");
        if (handler == null) {
            throw new IllegalArgumentException("handler는 필수입니다.");
        }
        handlers.computeIfAbsent(resolvedDestination, ignored -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public List<CpfBrokerBridgeMessage> findRecent(String destination, int limit) {
        String resolvedDestination = hasText(destination) ? destination : null;
        int resolvedLimit = Math.max(1, Math.min(limit <= 0 ? 50 : limit, recentLimit()));
        return recentMessages.stream()
                .filter(message -> resolvedDestination == null || resolvedDestination.equals(message.destination()))
                .sorted(Comparator.comparing(CpfBrokerBridgeMessage::createdAt).reversed())
                .limit(resolvedLimit)
                .toList();
    }

    private void publishToAdapter(CpfBrokerBridgeMessage message) {
        try {
            if ("IN_MEMORY".equals(message.broker())) {
                return;
            }
            String json = objectMapper.writeValueAsString(message);
            if ("KAFKA".equals(message.broker())) {
                KafkaTemplate<String, String> template = kafkaTemplateProvider.getIfAvailable();
                if (template == null) {
                    throw new IllegalStateException("KafkaTemplate bean이 없습니다.");
                }
                template.send(message.destination(), message.key(), json);
                return;
            }
            if ("RABBIT".equals(message.broker())) {
                RabbitTemplate template = rabbitTemplateProvider.getIfAvailable();
                if (template == null) {
                    throw new IllegalStateException("RabbitTemplate bean이 없습니다.");
                }
                String exchange = environment.getProperty("cpf.broker.rabbit.exchange", "cpf.exchange");
                template.convertAndSend(exchange, message.destination(), json, raw -> {
                    MessageProperties properties = raw.getMessageProperties();
                    message.headers().forEach(properties::setHeader);
                    properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    return raw;
                });
                return;
            }
            throw new IllegalArgumentException("지원하지 않는 broker 종류입니다: " + message.broker());
        } catch (JsonProcessingException ex) {
            throw new CpfExternalServiceException("broker payload 직렬화에 실패했습니다.", ex);
        } catch (RuntimeException ex) {
            throw new CpfExternalServiceException(
                    "broker 발행에 실패했습니다. broker=" + message.broker()
                            + ", destination=" + message.destination(),
                    ex);
        }
    }

    private Map<String, String> propagationHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.putAll(TransactionContext.propagationHeaders());
        headers.putAll(CpfWorkflowContext.propagationHeaders());
        if (additionalHeaders != null) {
            additionalHeaders.forEach((name, value) -> {
                if (hasText(name) && value != null) {
                    headers.put(name, value);
                }
            });
        }
        return Map.copyOf(headers);
    }

    private void dispatchLocal(CpfBrokerBridgeMessage message) {
        List<CpfBrokerBridgeHandler> targets = new ArrayList<>();
        targets.addAll(handlers.getOrDefault(message.destination(), new CopyOnWriteArrayList<>()));
        targets.addAll(handlers.getOrDefault("*", new CopyOnWriteArrayList<>()));
        targets.forEach(handler -> handler.handle(message));
    }

    private void remember(CpfBrokerBridgeMessage message) {
        recentMessages.addFirst(message);
        while (recentMessages.size() > recentLimit()) {
            recentMessages.pollLast();
        }
    }

    private boolean enabled() {
        return environment.getProperty(
                "cpf.broker.enabled",
                Boolean.class,
                true);
    }

    private String brokerType() {
        String value = environment.getProperty(
                "cpf.broker.type",
                "IN_MEMORY");
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private int recentLimit() {
        return Math.max(1, environment.getProperty(
                "cpf.broker.recent-message-limit",
                Integer.class,
                DEFAULT_RECENT_LIMIT));
    }

    private String requiredText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
