package cpf.cmn.mqe.service;

import cpf.cmn.mqe.config.CmnMessagingProperties;
import cpf.cmn.mqe.core.CmnMessageBrokerType;
import cpf.cmn.mqe.core.CmnMessageConsumer;
import cpf.cmn.mqe.core.CmnMessageEnvelope;
import cpf.cmn.mqe.core.CmnMessageHandler;
import cpf.cmn.mqe.core.CmnMessagePublishResult;
import cpf.cmn.mqe.core.CmnMessagePublisher;
import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfExternalServiceException;
import cpf.pfw.common.logging.TransactionContext;
import cpf.pfw.common.workflow.CpfWorkflowContext;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * CMN 메시지 envelope 교육/프로젝트 helper 서비스입니다.
 *
 * <p>CPF-OWNERSHIP:PFW_PORT_MIGRATION_CANDIDATE</p>
 * <p>메시지 envelope, topic naming, 최근 메시지 조회 helper는 CMN에 둘 수 있지만,
 * Kafka/RabbitMQ client 직접 사용은 PFW broker port adapter로 이동해야 하는 기술 engine 후보입니다.
 * 이번 단계에서는 기존 샘플 호환성을 유지하고 architecture scan과 gap에 후속 이동 대상으로 남깁니다.</p>
 */
@Service
public class CmnMessageBridgeService implements CmnMessagePublisher, CmnMessageConsumer {
    private final CmnMessagingProperties properties;
    private final CmnMessageCodec codec;
    private final ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider;
    private final ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    private final ConcurrentMap<String, CopyOnWriteArrayList<CmnMessageHandler>> handlers = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<CmnMessageEnvelope> recentMessages = new ConcurrentLinkedDeque<>();

    public CmnMessageBridgeService(
            CmnMessagingProperties properties,
            CmnMessageCodec codec,
            ObjectProvider<KafkaTemplate<String, String>> kafkaTemplateProvider,
            ObjectProvider<RabbitTemplate> rabbitTemplateProvider) {
        this.properties = properties;
        this.codec = codec;
        this.kafkaTemplateProvider = kafkaTemplateProvider;
        this.rabbitTemplateProvider = rabbitTemplateProvider;
    }

    @Override
    public CmnMessagePublishResult publish(String key, Object payload) {
        return publish(properties.getDefaultDestination(), key, payload, Map.of());
    }

    @Override
    public CmnMessagePublishResult publish(String destination, String key, Object payload, Map<String, String> headers) {
        String resolvedDestination = TextUtils.defaultIfBlank(destination, properties.getDefaultDestination());
        String resolvedKey = TextUtils.defaultIfBlank(key, TransactionContext.getOrCreateTransactionId());

        if (!properties.isEnabled()) {
            return new CmnMessagePublishResult(
                    false,
                    properties.getBroker().name(),
                    resolvedDestination,
                    resolvedKey,
                    TransactionContext.getOrCreateTransactionId(),
                    "CPF 처리 기준입니다.");
        }

        CmnMessageEnvelope envelope = new CmnMessageEnvelope(
                properties.getBroker().name(),
                resolvedDestination,
                resolvedKey,
                payload,
                buildHeaders(headers),
                DateTimeUtils.nowDateTimeMillis());

        remember(envelope);
        publishByBroker(envelope);

        return new CmnMessagePublishResult(
                true,
                properties.getBroker().name(),
                resolvedDestination,
                resolvedKey,
                envelope.headers().get(TransactionContext.HEADER_TRANSACTION_ID),
                "CPF 처리 기준입니다.");
    }

    @Override
    public void subscribe(String destination, CmnMessageHandler handler) {
        String resolvedDestination = TextUtils.defaultIfBlank(destination, properties.getDefaultDestination());
        handlers.computeIfAbsent(resolvedDestination, key -> new CopyOnWriteArrayList<>()).add(handler);
    }

    @Override
    public List<CmnMessageEnvelope> findRecentMessages(String destination, int limit) {
        int resolvedLimit = limit <= 0 ? 50 : Math.min(limit, properties.getRecentMessageLimit());
        String resolvedDestination = TextUtils.defaultIfBlank(destination, null);

        return recentMessages.stream()
                .filter(envelope -> resolvedDestination == null || resolvedDestination.equals(envelope.destination()))
                .sorted(Comparator.comparing(CmnMessageEnvelope::createdAt).reversed())
                .limit(resolvedLimit)
                .toList();
    }

    private Map<String, String> buildHeaders(Map<String, String> additionalHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();

        // CPF 기능 설명입니다.
        headers.putAll(TransactionContext.propagationHeaders());

        // CPF 기능 설명입니다.
        headers.putAll(CpfWorkflowContext.propagationHeaders());

        if (additionalHeaders != null) {
            additionalHeaders.forEach((key, value) -> {
                if (TextUtils.hasText(key) && value != null) {
                    headers.put(key, value);
                }
            });
        }
        return headers;
    }

    private void publishByBroker(CmnMessageEnvelope envelope) {
        CmnMessageBrokerType broker = properties.getBroker();
        if (broker == CmnMessageBrokerType.IN_MEMORY) {
            dispatchToLocalHandlers(envelope);
            return;
        }

        String jsonMessage = codec.toJson(envelope);
        try {
            if (broker == CmnMessageBrokerType.KAFKA) {
                KafkaTemplate<String, String> kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
                if (kafkaTemplate == null) {
                    throw new IllegalStateException("CPF 처리 기준입니다.");
                }
                kafkaTemplate.send(envelope.destination(), envelope.key(), jsonMessage);
                return;
            }

            if (broker == CmnMessageBrokerType.RABBIT) {
                RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
                if (rabbitTemplate == null) {
                    throw new IllegalStateException("CPF 처리 기준입니다.");
                }
                rabbitTemplate.convertAndSend(
                        properties.getRabbit().getExchange(),
                        TextUtils.defaultIfBlank(envelope.destination(), properties.getRabbit().getRoutingKey()),
                        jsonMessage,
                        message -> {
                            MessageProperties messageProperties = message.getMessageProperties();
                            envelope.headers().forEach(messageProperties::setHeader);
                            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                            return message;
                        });
            }
        } catch (RuntimeException ex) {
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + broker
                    + ", destination=" + envelope.destination(), ex);
        }
    }

    private void dispatchToLocalHandlers(CmnMessageEnvelope envelope) {
        List<CmnMessageHandler> destinationHandlers = new ArrayList<>();
        destinationHandlers.addAll(handlers.getOrDefault(envelope.destination(), new CopyOnWriteArrayList<>()));
        destinationHandlers.addAll(handlers.getOrDefault("*", new CopyOnWriteArrayList<>()));
        destinationHandlers.forEach(handler -> handler.handle(envelope));
    }

    private void remember(CmnMessageEnvelope envelope) {
        recentMessages.addFirst(envelope);
        int limit = Math.max(1, properties.getRecentMessageLimit());
        while (recentMessages.size() > limit) {
            recentMessages.pollLast();
        }
    }
}
