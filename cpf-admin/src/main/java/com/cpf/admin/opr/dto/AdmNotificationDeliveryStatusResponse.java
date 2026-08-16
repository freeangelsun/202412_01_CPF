package com.cpf.admin.opr.dto;

import java.time.LocalDateTime;

/** 알림 Durable Outbox의 CAS·lease·retry 상태 Snapshot입니다. */
public record AdmNotificationDeliveryStatusResponse(
        long deliveryId,
        String operationId,
        String requestHash,
        String deliveryStatus,
        int attemptCount,
        int maxAttempts,
        LocalDateTime nextAttemptAt,
        String leaseOwner,
        LocalDateTime leaseUntil,
        long version,
        String lastErrorCode,
        String updatedBy,
        LocalDateTime updatedAt) {
}
