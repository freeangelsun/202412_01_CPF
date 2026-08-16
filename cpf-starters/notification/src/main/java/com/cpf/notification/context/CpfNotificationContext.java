package com.cpf.notification.context;

/** Notification Owner 내부의 dispatch 메타데이터입니다. Core Context 확장값이 아닙니다. */
public record CpfNotificationContext(
        String notificationId,
        String channel,
        String templateId,
        String provider,
        String recipientHash,
        int attempt,
        String outboxId,
        String unknownOutcomeId) {
    public CpfNotificationContext {
        if (attempt < 1) throw new IllegalArgumentException("attempt");
    }
}
