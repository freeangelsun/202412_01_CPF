package com.cpf.reference.messaging;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * REF 변경 이벤트를 CPF broker reliability 경로로 실제 발행하는 교육 샘플입니다.
 *
 * <p>{@link #publishPlan(String, String)}은 발행 전 envelope를 확인하는 용도이고,
 * {@link #publish(String, String)}은 provider-neutral {@link CpfBrokerClient}를 호출해
 * 실제 Outbox/Broker Provider 경로로 요청을 전달합니다. Provider가 결과를 확정하지
 * 못하면 {@code UNKNOWN} 상태를 그대로 반환하며 호출자가 조회·대사 흐름을 이어갈 수
 * 있도록 결과를 임의로 성공 처리하지 않습니다.</p>
 */
public class ReferenceBrokerPublishEducationSample {

    private final CpfBrokerClient brokerClient;

    public ReferenceBrokerPublishEducationSample(CpfBrokerClient brokerClient) {
        this.brokerClient = Objects.requireNonNull(brokerClient, "brokerClient must not be null");
    }

    public CpfBrokerEnvelope publishPlan(String transactionId, String idempotencyKey) {
        String resolvedTransactionId = require(transactionId, "transactionId");
        String resolvedIdempotencyKey = require(idempotencyKey, "idempotencyKey");
        CpfBrokerMessage message = new CpfBrokerMessage(
                "REF-" + resolvedIdempotencyKey,
                "com.cpf.reference.changed",
                resolvedIdempotencyKey,
                "{\"eventType\":\"REF_CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                Map.of("x-cpf-transaction-id", resolvedTransactionId));
        return new CpfBrokerEnvelope(
                resolvedTransactionId,
                resolvedTransactionId + "-BROKER",
                "REF",
                "REF",
                resolvedIdempotencyKey,
                Instant.now(),
                message,
                Map.of("sampleId", "REF Reference-MSG-001"));
    }

    public CpfBrokerPublishResult publish(String transactionId, String idempotencyKey) {
        CpfBrokerEnvelope envelope = publishPlan(transactionId, idempotencyKey);
        CpfBrokerMessage message = envelope.message();
        CpfBrokerPublishRequest request = new CpfBrokerPublishRequest(
                message.messageId(),
                message.topic(),
                message.key(),
                message.payload(),
                message.contentType(),
                envelope.transactionId(),
                envelope.segmentId(),
                envelope.producerModule(),
                envelope.consumerModule(),
                envelope.idempotencyKey(),
                message.headers(),
                envelope.attributes());
        return brokerClient.enqueue(request);
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
