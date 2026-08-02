package com.cpf.starter.notification;

import java.time.Instant;
import java.util.Map;

/** Provider-neutral notification request. Transport SDK types are intentionally absent. */
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
        notificationId = require(notificationId, "notificationId");
        channel = require(channel, "channel");
        recipient = require(recipient, "recipient");
        templateId = require(templateId, "templateId");
        idempotencyKey = require(idempotencyKey, "idempotencyKey");
        transactionId = require(transactionId, "transactionId");
        variables = Map.copyOf(variables == null ? Map.of() : variables);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
