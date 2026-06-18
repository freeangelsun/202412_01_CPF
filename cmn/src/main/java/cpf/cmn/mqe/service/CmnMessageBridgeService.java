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
 * Kafka/RabbitMQ/?몃찓⑤━ 釉뚮줈而ㅻ? 怨듯넻 ?명꽣?섏씠?ㅻ줈 媛먯떥??硫붿떆吏??쒕퉬?ㅼ엯?덈떎.
 *
 * <p>?낅Т 媛쒕컻?먮뒗 ???쒕퉬?ㅻ? 吏곸젒 援ы쁽?섏? ?딄퀬 {@link CmnMessagePublisher}?
 * {@link CmnMessageConsumer}瑜?二쇱엯諛쏆븘 ?ъ슜?⑸땲?? 硫붿떆吏?먮뒗 ?꾩옱 PFW 嫄곕옒 ?ㅻ뜑?
 * ?뚰겕?뚮줈???ㅻ뜑媛 ?먮룞?쇰줈 ?ㅻ━誘濡? HTTP ?몄텧怨?MQ 諛쒗뻾??媛숈? 湲濡쒕쾶 嫄곕옒ID濡?異붿쟻?⑸땲??</p>
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
                    "CMN 硫붿떆吏?湲곕뒫??鍮꾪솢?깊솕?섏뼱 硫붿떆吏瑜?諛쒗뻾?섏? ?딆븯?듬땲??");
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
                "硫붿떆吏瑜?諛쒗뻾?덉뒿?덈떎.");
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

        // HTTP ?몄텧怨??숈씪?섍쾶 PFW 嫄곕옒 ?ㅻ뜑瑜?MQ 硫붿떆吏?먮룄 ?ㅼ뼱 ??援ш컙 異붿쟻??媛?ν븯寃??⑸땲??
        headers.putAll(TransactionContext.propagationHeaders());

        // ?뚰겕?뚮줈??蹂댁긽 異붿쟻 ?ㅻ뜑媛 ?덉쑝硫?MQ ?꾩쿂由ъ뿉?쒕룄 媛숈? ?뚰겕?뚮줈???몄뒪?댁뒪濡??댁뼱吏묐땲??
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
                    throw new IllegalStateException("KafkaTemplate Bean???놁뒿?덈떎.");
                }
                kafkaTemplate.send(envelope.destination(), envelope.key(), jsonMessage);
                return;
            }

            if (broker == CmnMessageBrokerType.RABBIT) {
                RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
                if (rabbitTemplate == null) {
                    throw new IllegalStateException("RabbitTemplate Bean???놁뒿?덈떎.");
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
            throw new CpfExternalServiceException("MQ 硫붿떆吏 諛쒗뻾???ㅽ뙣?덉뒿?덈떎. broker=" + broker
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

