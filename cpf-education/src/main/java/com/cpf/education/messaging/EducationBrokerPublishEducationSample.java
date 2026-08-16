package com.cpf.education.messaging;

import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;

/**
 * EDU 변경 이벤트를 CPF의 Provider-neutral Broker 공개 API로 발행하는 교육 샘플입니다.
 *
 * <p>업무 코드는 내부 Envelope/Outbox/Worker 구현을 직접 참조하지 않습니다. 공개
 * {@link CpfBrokerPublishRequest}와 {@link CpfBrokerClient}만 사용하며, CPF Runtime이
 * Outbox 저장·Provider I/O·UNKNOWN 판정·재조정을 소유합니다.</p>
 */
public class EducationBrokerPublishEducationSample {

    private final CpfBrokerClient brokerClient;

    public EducationBrokerPublishEducationSample(CpfBrokerClient brokerClient) {
        this.brokerClient = Objects.requireNonNull(brokerClient, "brokerClient must not be null");
    }

    /** 발행 전에 추적·멱등·업무 Payload 계약을 확인할 수 있는 공개 요청을 만듭니다. */
    public CpfBrokerPublishRequest publishPlan(String transactionId, String idempotencyKey) {
        String resolvedTransactionId = require(transactionId, "transactionId");
        String resolvedIdempotencyKey = require(idempotencyKey, "idempotencyKey");
        return new CpfBrokerPublishRequest(
                "EDU-" + resolvedIdempotencyKey,
                "com.cpf.education.changed",
                resolvedIdempotencyKey,
                "{\"eventType\":\"EDU_CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                resolvedTransactionId,
                resolvedTransactionId + "-BROKER",
                "EDU",
                "EDU",
                resolvedIdempotencyKey,
                Map.of("x-cpf-transaction-id", resolvedTransactionId),
                Map.of("sampleId", "EDU-MSG-001"));
    }

    /** CPF 공개 Broker Client에 enqueue하고 PUBLISHED/FAILED/UNKNOWN 결과를 그대로 보존합니다. */
    public CpfBrokerPublishResult publish(String transactionId, String idempotencyKey) {
        return brokerClient.enqueue(publishPlan(transactionId, idempotencyKey));
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }
}
