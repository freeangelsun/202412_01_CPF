package com.cpf.core.common.broker;

import java.time.Instant;

/**
 * DLQ 메시지 재처리 요청 DTO입니다.
 */
public record CpfBrokerDlqReplayRequest(
        String topic,
        String messageId,
        String requestedBy,
        String auditReason,
        Instant requestedAt) {

    public CpfBrokerDlqReplayRequest {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("messageId는 필수입니다.");
        }
        requestedAt = requestedAt == null ? Instant.now() : requestedAt;
    }
}
