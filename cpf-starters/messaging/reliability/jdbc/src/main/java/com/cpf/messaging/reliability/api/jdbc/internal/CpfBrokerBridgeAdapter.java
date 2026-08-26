package com.cpf.messaging.reliability.api.jdbc.internal;

import com.cpf.messaging.api.CpfBrokerBridgeHandler;
import com.cpf.messaging.api.CpfBrokerBridgeMessage;
import com.cpf.messaging.api.CpfBrokerBridgePort;
import com.cpf.messaging.api.CpfBrokerBridgeResult;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import com.cpf.foundation.context.header.CpfHeaderNames;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 동일 JVM Local Bridge만 제공하는 topology-independent 기본 Adapter입니다.
 *
 * <p>Kafka·AMQP 같은 원격 Broker는 해당 Starter의 {@code CpfMessagingTemplate}가 소유합니다.
 * Core에서 원격 Broker를 선택하면 로컬 fallback하지 않고 즉시 실패합니다.</p>
 */
@Component
@ConditionalOnProperty(name = "cpf.broker.type", havingValue = "IN_MEMORY", matchIfMissing = true)
public class CpfBrokerBridgeAdapter implements CpfBrokerBridgePort {
    private static final int DEFAULT_RECENT_LIMIT = 200;

    private final Environment environment;
    private final CpfMessageBridgeContextSupport contextSupport;
    private final ConcurrentMap<String, CopyOnWriteArrayList<CpfBrokerBridgeHandler>> handlers =
            new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<CpfBrokerBridgeMessage> recentMessages =
            new ConcurrentLinkedDeque<>();

    /** CpfBrokerBridgeAdapter 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfBrokerBridgeAdapter(Environment environment, CpfMessageBridgeContextSupport contextSupport) {
        this.environment = environment;
        this.contextSupport = contextSupport;
    }

    @Override
    public CpfBrokerBridgeResult publish(
            String destination,
            String key,
            Object payload,
            Map<String, String> additionalHeaders) {
        String resolvedDestination = requiredText(destination, "destination");
        String resolvedKey = hasText(key) ? key : CpfContexts.transactionId();
        requireLocalTopology();
        Map<String, String> headers = contextSupport.prepareOutbound("IN_MEMORY", resolvedDestination, resolvedKey, additionalHeaders).headers();
        CpfBrokerBridgeMessage message = new CpfBrokerBridgeMessage(
                "IN_MEMORY",
                resolvedDestination,
                resolvedKey,
                payload,
                headers,
                Instant.now());

        if (!enabled()) {
            return new CpfBrokerBridgeResult(
                    false,
                    "IN_MEMORY",
                    resolvedDestination,
                    resolvedKey,
                    CpfContexts.transactionId(),
                    "broker bridge가 설정으로 비활성화되어 있습니다.");
        }

        remember(message);
        dispatchLocal(message);
        return new CpfBrokerBridgeResult(
                true,
                "IN_MEMORY",
                resolvedDestination,
                resolvedKey,
                headers.get(CpfHeaderNames.TRANSACTION_ID),
                "동일 JVM local bridge 발행이 완료됐습니다.");
    }

    @Override
    public void subscribe(String destination, CpfBrokerBridgeHandler handler) {
        String resolvedDestination = requiredText(destination, "destination");
        if (handler == null) {
            throw new IllegalArgumentException("handler는 필수입니다.");
        }
        handlers.computeIfAbsent(resolvedDestination, ignored -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    @Override
    public List<CpfBrokerBridgeMessage> findRecent(String destination, int limit) {
        String resolvedDestination = hasText(destination) ? destination : null;
        int resolvedLimit = Math.max(1, Math.min(limit <= 0 ? 50 : limit, recentLimit()));
        return recentMessages.stream()
                .filter(message -> resolvedDestination == null
                        || resolvedDestination.equals(message.destination()))
                .sorted(Comparator.comparing(value -> value.createdAt()).reversed())
                .limit(resolvedLimit)
                .toList();
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

    private void requireLocalTopology() {
        String broker = environment.getProperty("cpf.broker.type", "IN_MEMORY")
                .trim()
                .toUpperCase(Locale.ROOT);
        if (!"IN_MEMORY".equals(broker)) {
            throw new IllegalStateException(
                    "Remote broker requires the matching CPF messaging Starter; Core local bridge does not fallback: "
                            + broker);
        }
    }

    private boolean enabled() {
        return environment.getProperty("cpf.broker.enabled", Boolean.class, true);
    }

    private int recentLimit() {
        return Math.max(1, environment.getProperty(
                "cpf.broker.recent-message-limit",
                Integer.class,
                DEFAULT_RECENT_LIMIT));
    }

    private static String requiredText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
