package com.cpf.starter.notification;

import java.time.Instant;
import java.util.Locale;

/** Provider-neutral result with explicit UNKNOWN_RESULT support. */
public record CpfNotificationResult(
        String notificationId,
        String provider,
        String status,
        String providerMessageId,
        String detail,
        Instant processedAt) {

    public CpfNotificationResult {
        notificationId = require(notificationId, "notificationId");
        status = require(status, "status").toUpperCase(Locale.ROOT);
        provider = optional(provider);
        if (("SENT".equals(status) || "UNKNOWN_RESULT".equals(status)) && provider == null) {
            throw new IllegalArgumentException("provider is required for status " + status);
        }
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }

    public static CpfNotificationResult sent(
            String notificationId, String provider, String providerMessageId) {
        return sentAt(notificationId, provider, providerMessageId, Instant.now());
    }

    public static CpfNotificationResult sentAt(
            String notificationId,
            String provider,
            String providerMessageId,
            Instant processedAt) {
        return new CpfNotificationResult(
                notificationId, provider, "SENT", providerMessageId, null, processedAt);
    }

    public static CpfNotificationResult unknown(
            String notificationId, String provider, String detail) {
        return unknownAt(notificationId, provider, detail, Instant.now());
    }

    public static CpfNotificationResult unknownAt(
            String notificationId, String provider, String detail, Instant processedAt) {
        return new CpfNotificationResult(
                notificationId, provider, "UNKNOWN_RESULT", null, detail, processedAt);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }

    private static String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
