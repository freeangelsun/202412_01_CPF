package com.cpf.education.messaging;

import com.cpf.messaging.api.CpfBrokerClient;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import com.cpf.messaging.api.CpfBrokerPublishResultProbe;
import com.cpf.messaging.api.CpfBrokerUnknownResultPort;

import java.util.Objects;

/**
 * 내부 Outbox/Worker 구현을 노출하지 않고 CPF 공개 Messaging 계약으로 발행·UNKNOWN 재조정을 보여주는 샘플입니다.
 */
public class EducationOutboxInboxEducationSample {
    private final CpfBrokerClient brokerClient;
    private final CpfBrokerUnknownResultPort unknownResultPort;

    public EducationOutboxInboxEducationSample(
            CpfBrokerClient brokerClient,
            CpfBrokerUnknownResultPort unknownResultPort) {
        this.brokerClient = Objects.requireNonNull(brokerClient, "brokerClient must not be null");
        this.unknownResultPort = Objects.requireNonNull(unknownResultPort, "unknownResultPort must not be null");
    }

    /** Runtime의 Outbox 경로에 enqueue합니다. 내부 Repository/Worker 타입은 업무 코드에 노출하지 않습니다. */
    public CpfBrokerPublishResult publish(CpfBrokerPublishRequest request) {
        return brokerClient.enqueue(Objects.requireNonNull(request, "request must not be null"));
    }

    /** 응답 유실 등 UNKNOWN 결과를 공개 Reconcile Port로 재확인합니다. */
    public CpfBrokerPublishResult reconcile(
            CpfBrokerPublishRequest request,
            String operatorId,
            String reason) {
        Objects.requireNonNull(request, "request must not be null");
        return unknownResultPort.reconcile(
                new CpfBrokerPublishResultProbe(request.idempotencyKey(), request.transactionId(), request.topic()),
                require(operatorId, "operatorId"),
                require(reason, "reason"));
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
