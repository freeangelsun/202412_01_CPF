package com.cpf.core.internal.broker;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerResult;

import java.time.Instant;
import java.util.Objects;

/** Public broker API를 기존 Outbox Runtime에 연결하는 Core 내부 adapter입니다. */
public final class CpfBrokerClientAdapter implements CpfBrokerClient {
    private final CpfBrokerOutboxPort outbox;

    public CpfBrokerClientAdapter(CpfBrokerOutboxPort outbox) {
        this.outbox = Objects.requireNonNull(outbox, "outbox");
    }

    @Override
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                request.messageId(),
                request.topic(),
                request.key(),
                request.payload(),
                request.contentType(),
                request.headers());
        CpfBrokerResult result = outbox.saveOutbox(new CpfBrokerEnvelope(
                request.transactionId(),
                request.segmentId(),
                request.producerModule(),
                request.consumerModule(),
                request.idempotencyKey(),
                Instant.now(),
                message,
                request.attributes()));
        return new CpfBrokerPublishResult(
                result.status(),
                result.messageId(),
                result.brokerName(),
                result.partitionKey(),
                result.processedAt(),
                result.detail());
    }
}
