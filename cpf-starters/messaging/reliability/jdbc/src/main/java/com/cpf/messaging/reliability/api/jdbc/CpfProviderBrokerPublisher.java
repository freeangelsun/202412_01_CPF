package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.spi.broker.CpfBrokerEnvelope;
import com.cpf.messaging.spi.broker.CpfBrokerFailureSanitizer;
import com.cpf.messaging.spi.broker.CpfBrokerPublisher;
import com.cpf.messaging.spi.broker.CpfBrokerResult;
import com.cpf.messaging.context.*;
import java.time.Clock;
import java.util.Objects;

/** Internal worker adapter that performs the actual Provider publish after an outbox claim. */
/** CpfProviderBrokerPublisher는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
public final class CpfProviderBrokerPublisher implements CpfBrokerPublisher {
    private final CpfMessagingTemplateRouter router;
    private final Clock clock;
    private final CpfMessageBridgeContextSupport contextSupport;

    /** CpfProviderBrokerPublisher 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfProviderBrokerPublisher(CpfMessagingTemplateRouter router, Clock clock, CpfMessageBridgeContextSupport contextSupport) {
        this.router = Objects.requireNonNull(router, "router");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.contextSupport = Objects.requireNonNull(contextSupport, "contextSupport");
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
            var bundle = contextSupport.extractInbound("OUTBOX", envelope.message().messageId(), envelope.message().topic(), envelope.producerModule(), envelope.consumerModule(), null, null, 1, false, null, null, envelope.message().headers(), null);
            final CpfBrokerPublishResult[] holder = new CpfBrokerPublishResult[1];
            contextSupport.consume(bundle, () -> holder[0] = router.send(request));
            providerResult = holder[0];
        // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
        } catch (IllegalArgumentException | SecurityException deterministicFailure) {
            return CpfBrokerResult.failed(envelope.message().messageId(), "VALIDATION",
                    CpfBrokerFailureSanitizer.sanitize(deterministicFailure.getMessage()));
        // 실패를 성공으로 오인하지 않고 재시도 가능 여부와 결과불명 복구 경로를 보존합니다.
        } catch (RuntimeException uncertainProviderFailure) {
            return unknown(envelope, "Provider publish raised no definitive result: "
                    + CpfBrokerFailureSanitizer.sanitizeNullable(
                            uncertainProviderFailure.getMessage()));
        }
        if (providerResult == null) {
            return unknown(envelope, "Provider returned no result");
        }
        if (providerResult.messageId() != null
                && !envelope.message().messageId().equals(providerResult.messageId())) {
            return unknown(envelope, "Provider result correlation mismatch");
        }
        if (!isRecognizedProviderStatus(providerResult.status())) {
            return unknown(envelope, "Provider returned unsupported status: "
                    + CpfBrokerFailureSanitizer.sanitizeNullable(providerResult.status()));
        }
        return new CpfBrokerResult(
                providerResult.status(),
                envelope.message().messageId(),
                providerResult.brokerName(),
                providerResult.partitionKey(),
                providerResult.processedAt() == null ? clock.instant() : providerResult.processedAt(),
                CpfBrokerFailureSanitizer.sanitizeNullable(providerResult.detail()));
    }

    private static boolean isRecognizedProviderStatus(String status) {
        return "PUBLISHED".equalsIgnoreCase(status)
                || "FAILED".equalsIgnoreCase(status)
                || "UNKNOWN".equalsIgnoreCase(status)
                || "RESULT_UNKNOWN".equalsIgnoreCase(status);
    }

    private CpfBrokerResult unknown(CpfBrokerEnvelope envelope, String detail) {
        return new CpfBrokerResult(
                "UNKNOWN", envelope.message().messageId(), "UNKNOWN_PROVIDER", null,
                clock.instant(), CpfBrokerFailureSanitizer.sanitize(detail));
    }
}
