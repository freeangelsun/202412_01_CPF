package com.cpf.core.common.broker;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;

import java.time.Instant;

/** Public broker API를 기존 Outbox Runtime에 연결하는 내부 adapter입니다. */
public final class CpfBrokerClientAdapter implements CpfBrokerClient {
    private final CpfBrokerOutboxPort outbox;
    public CpfBrokerClientAdapter(CpfBrokerOutboxPort outbox){ this.outbox=java.util.Objects.requireNonNull(outbox); }
    @Override public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest r){
        CpfBrokerMessage message=new CpfBrokerMessage(r.messageId(),r.topic(),r.key(),r.payload(),r.contentType(),r.headers());
        CpfBrokerResult result=outbox.saveOutbox(new CpfBrokerEnvelope(r.transactionId(),r.segmentId(),r.producerModule(),r.consumerModule(),r.idempotencyKey(),Instant.now(),message,r.attributes()));
        return new CpfBrokerPublishResult(result.status(),result.messageId(),result.brokerName(),result.partitionKey(),result.processedAt(),result.detail());
    }
}
