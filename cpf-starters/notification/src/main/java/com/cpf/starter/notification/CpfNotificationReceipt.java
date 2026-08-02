package com.cpf.starter.notification;

import java.time.Instant;
import java.util.Objects;

public record CpfNotificationReceipt(
        String receiptId,
        String notificationId,
        String provider,
        String receiptStatus,
        String detail,
        Instant receivedAt) {
    public CpfNotificationReceipt {
        receiptId = requireText(receiptId, "receiptId");
        notificationId = requireText(notificationId, "notificationId");
        provider = requireText(provider, "provider");
        receiptStatus = requireText(receiptStatus, "receiptStatus").toUpperCase(java.util.Locale.ROOT);
        if (!java.util.Set.of("DELIVERED", "BOUNCED", "REJECTED", "ACCEPTED", "UNKNOWN")
                .contains(receiptStatus)) {
            throw new IllegalArgumentException("unsupported receiptStatus: " + receiptStatus);
        }
        Objects.requireNonNull(receivedAt, "receivedAt");
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
