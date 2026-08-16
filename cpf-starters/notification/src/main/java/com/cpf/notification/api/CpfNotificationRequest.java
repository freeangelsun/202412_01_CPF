package com.cpf.notification.api;

import java.time.Instant;
import java.util.Map;

/** Provider-neutral notification command. */
/** CpfNotificationRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfNotificationRequest(
        String notificationId,
        String channel,
        String recipient,
        String templateId,
        Map<String, String> variables,
        String idempotencyKey,
        String transactionId,
        Instant notBefore) {
    public CpfNotificationRequest {
        notificationId = required(notificationId, "notificationId");
        channel = required(channel, "channel").toUpperCase(java.util.Locale.ROOT);
        recipient = required(recipient, "recipient");
        templateId = required(templateId, "templateId");
        idempotencyKey = required(idempotencyKey, "idempotencyKey");
        transactionId = required(transactionId, "transactionId");
        variables = Map.copyOf(variables == null ? Map.of() : variables);
    }
    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }
}
