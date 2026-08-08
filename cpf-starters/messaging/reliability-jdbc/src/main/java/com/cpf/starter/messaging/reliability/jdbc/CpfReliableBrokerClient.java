package com.cpf.starter.messaging.reliability.jdbc;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerResult;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.transaction.annotation.Transactional;

/**
 * Public broker client that persists the request to the transactional outbox.
 * Provider I/O is deliberately deferred to {@code CpfBrokerPublisherWorker}.
 */
public class CpfReliableBrokerClient implements CpfBrokerClient {
    private final CpfBrokerOutboxPort outbox;
    private final Clock clock;

    public CpfReliableBrokerClient(CpfBrokerOutboxPort outbox, Clock clock) {
        this.outbox = Objects.requireNonNull(outbox, "outbox");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    @Transactional(transactionManager = "cpfTransactionManager")
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        CpfBrokerPublishRequest validated = CpfBrokerHeaderPolicy.validatedRequest(request);
        requireTracking(validated.transactionId(), "transactionId");
        requireTracking(validated.idempotencyKey(), "idempotencyKey");
        Instant occurredAt = clock.instant();
        CpfBrokerMessage message = new CpfBrokerMessage(
                validated.messageId(), validated.topic(), validated.key(), validated.payload(),
                validated.contentType(), validated.headers());
        CpfBrokerEnvelope envelope = new CpfBrokerEnvelope(
                validated.transactionId(), validated.segmentId(), validated.producerModule(),
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
