package com.cpf.starter.messaging.reliability;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerFailureSanitizer;
import com.cpf.core.common.broker.CpfBrokerPublisher;
import com.cpf.core.common.broker.CpfBrokerResult;
import java.time.Clock;
import java.util.Objects;

/** Internal worker adapter that performs the actual Provider publish after an outbox claim. */
public final class CpfProviderBrokerPublisher implements CpfBrokerPublisher {
    private final CpfBrokerClientRouter router;
    private final Clock clock;

    public CpfProviderBrokerPublisher(CpfBrokerClientRouter router, Clock clock) {
        this.router = Objects.requireNonNull(router, "router");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public CpfBrokerResult publish(CpfBrokerEnvelope envelope) {
        Objects.requireNonNull(envelope, "envelope");
        CpfBrokerPublishRequest request = new CpfBrokerPublishRequest(
                envelope.message().messageId(),
                envelope.message().topic(),
                envelope.message().key(),
                envelope.message().payload(),
                envelope.message().contentType(),
                envelope.transactionId(),
                envelope.segmentId(),
                envelope.producerModule(),
                envelope.consumerModule(),
                envelope.idempotencyKey(),
                envelope.message().headers(),
                envelope.attributes());
        CpfBrokerPublishResult providerResult;
        try {
            providerResult = router.enqueue(request);
        } catch (IllegalArgumentException | SecurityException deterministicFailure) {
            return CpfBrokerResult.failed(envelope.message().messageId(), "VALIDATION",
                    CpfBrokerFailureSanitizer.sanitize(deterministicFailure.getMessage()));
        }
        if (providerResult == null) {
            return unknown(envelope, "Provider returned no result");
        }
        if (providerResult.messageId() != null
                && !envelope.message().messageId().equals(providerResult.messageId())) {
            return unknown(envelope, "Provider result correlation mismatch");
        }
        return new CpfBrokerResult(
                providerResult.status(),
                envelope.message().messageId(),
                providerResult.brokerName(),
                providerResult.partitionKey(),
                providerResult.processedAt() == null ? clock.instant() : providerResult.processedAt(),
                CpfBrokerFailureSanitizer.sanitizeNullable(providerResult.detail()));
    }

    private CpfBrokerResult unknown(CpfBrokerEnvelope envelope, String detail) {
        return new CpfBrokerResult(
                "UNKNOWN", envelope.message().messageId(), "UNKNOWN_PROVIDER", null,
                clock.instant(), CpfBrokerFailureSanitizer.sanitize(detail));
    }
}
