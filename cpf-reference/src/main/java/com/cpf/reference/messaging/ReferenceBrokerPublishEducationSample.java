package com.cpf.reference.messaging;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Map;

/**
 * REF 변경 이벤트를 CPF broker envelope로 발행하는 샘플입니다.
 */
public class ReferenceBrokerPublishEducationSample {

    public CpfBrokerEnvelope publishPlan(String transactionGlobalId, String idempotencyKey) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                "REF-" + idempotencyKey,
                "com.cpf.reference.changed",
                idempotencyKey,
                "{\"eventType\":\"REF_CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                Map.of("x-cpf-transaction-global-id", transactionGlobalId));
        return new CpfBrokerEnvelope(
                transactionGlobalId,
                transactionGlobalId + "-BROKER",
                "REF",
                "MBR",
                idempotencyKey,
                Instant.now(),
                message,
                Map.of("sampleId", "REF Reference-MSG-001"));
    }
}
