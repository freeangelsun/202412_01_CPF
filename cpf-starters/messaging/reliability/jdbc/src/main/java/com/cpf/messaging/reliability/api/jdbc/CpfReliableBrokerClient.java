package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.spi.broker.CpfBrokerEnvelope;
import com.cpf.messaging.spi.broker.CpfBrokerMessage;
import com.cpf.messaging.spi.broker.CpfBrokerOutboxPort;
import com.cpf.messaging.spi.broker.CpfBrokerResult;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.messaging.context.CpfMessageBridgeContextSupport;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public broker client that persists the request to the transactional outbox.
 * Provider I/O is deliberately deferred to {@code CpfBrokerPublisherWorker}.
 */
/** CpfReliableBrokerClient는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
public class CpfReliableBrokerClient implements CpfMessagingTemplate {
    private final CpfBrokerOutboxPort outbox;
    private final Clock clock;
    private final CpfMessageBridgeContextSupport contextSupport;

    /** CpfReliableBrokerClient 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfReliableBrokerClient(CpfBrokerOutboxPort outbox, Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        this.outbox = Objects.requireNonNull(outbox, "outbox");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.contextSupport = Objects.requireNonNull(contextSupport, "contextSupport");
    }

    @Override
    // 메시지 상태 전이를 단일 트랜잭션으로 묶어 부분 저장과 중복 처리를 방지합니다.
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerPublishResult send(CpfBrokerPublishRequest request) {
        CpfBrokerPublishRequest validated = CpfBrokerHeaderPolicy.validatedRequest(request);
        requireTracking(validated.idempotencyKey(), "idempotencyKey");
        var current = CpfContexts.requireCurrent();
        String transactionId = current.transactionId();
        String segmentId = current.execution().segmentId();
        var outbound = contextSupport.prepareOutbound("OUTBOX", validated.topic(), validated.messageId(), validated.headers());
        Instant occurredAt = clock.instant();
        CpfBrokerMessage message = new CpfBrokerMessage(
                validated.messageId(), validated.topic(), validated.key(), validated.payload(),
                validated.contentType(), outbound.headers());
        CpfBrokerEnvelope envelope = new CpfBrokerEnvelope(
                transactionId, segmentId, validated.producerModule(),
                validated.consumerModule(), validated.idempotencyKey(), occurredAt,
                message, validated.attributes());
        CpfBrokerResult result = Objects.requireNonNull(
                outbox.saveOutbox(envelope), "outbox result");
        String status = isAccepted(result.status()) ? "ACCEPTED" : result.status();
        return new CpfBrokerPublishResult(
                status,
                validated.messageId(),
                result.brokerName() == null ? "CPF_OUTBOX" : result.brokerName(),
                result.partitionKey() == null ? validated.key() : result.partitionKey(),
                result.processedAt() == null ? occurredAt : result.processedAt(),
                result.detail());
    }

    private static String requireTracking(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required for reliable messaging");
        }
        return value.trim();
    }

    private static boolean isAccepted(String status) {
        return "ACCEPTED".equalsIgnoreCase(status)
                || "PENDING".equalsIgnoreCase(status)
                || "SAVED".equalsIgnoreCase(status);
    }
}
