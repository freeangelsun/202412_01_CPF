package com.cpf.messaging.api;

import java.time.Instant;
import java.util.Locale;

/**
 * Provider-neutral enqueue/publish result with explicit success, failure, and unknown states.
 * Core에서 Messaging Owner로 이동하되 기존 공개 record component를 보존합니다.
 */
public record CpfBrokerPublishResult(
        String status,
        String messageId,
        String brokerName,
        String partitionKey,
        Instant processedAt,
        String detail) {

    public CpfBrokerPublishResult {
        status = status == null || status.isBlank()
                ? "UNKNOWN"
                : status.trim().toUpperCase(Locale.ROOT);
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }

    /** 성공 상태 편의 조회입니다. */
    public boolean published() { return "PUBLISHED".equals(status); }

    /** 결과 확정 불가 상태 편의 조회입니다. */
    public boolean unknown() { return "UNKNOWN".equals(status); }
}
