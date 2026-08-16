package com.cpf.messaging.spi.broker;

import java.time.Instant;

/**
 * broker publish/consume 처리 결과입니다.
 */
public record CpfBrokerResult(
        String status,
        String messageId,
        String brokerName,
        String partitionKey,
        Instant processedAt,
        String detail) {

    public CpfBrokerResult {
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }

    /** accepted는 Broker 처리 결과를 표준 상태와 안전한 상세로 생성합니다. */
    public static CpfBrokerResult accepted(String messageId, String brokerName, String partitionKey) {
        return new CpfBrokerResult("ACCEPTED", messageId, brokerName, partitionKey, Instant.now(), null);
    }

    /** failed는 Broker 처리 결과를 표준 상태와 안전한 상세로 생성합니다. */
    public static CpfBrokerResult failed(String messageId, String brokerName, String detail) {
        return new CpfBrokerResult("FAILED", messageId, brokerName, null, Instant.now(), detail);
    }

    /** published는 Broker 처리 결과를 표준 상태와 안전한 상세로 생성합니다. */
    public static CpfBrokerResult published(String messageId, String brokerName, String partitionKey) {
        return new CpfBrokerResult("PUBLISHED", messageId, brokerName, partitionKey, Instant.now(), null);
    }

    /** consumed는 Broker 처리 결과를 표준 상태와 안전한 상세로 생성합니다. */
    public static CpfBrokerResult consumed(String messageId, String brokerName, String detail) {
        return new CpfBrokerResult("CONSUMED", messageId, brokerName, null, Instant.now(), detail);
    }
}
