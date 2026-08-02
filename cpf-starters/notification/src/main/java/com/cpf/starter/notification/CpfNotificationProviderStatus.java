package com.cpf.starter.notification;

/** Sanitized Provider health result used by CPF operations without exposing OSS types. */
public record CpfNotificationProviderStatus(String status, String reasonCode) {
    public CpfNotificationProviderStatus {
        status = require(status, "status").toUpperCase(java.util.Locale.ROOT);
        reasonCode = require(reasonCode, "reasonCode").toUpperCase(java.util.Locale.ROOT);
        if (!(status.equals("UP") || status.equals("DOWN")
                || status.equals("DEGRADED") || status.equals("UNKNOWN"))) {
            throw new IllegalArgumentException("unsupported notification Provider status: " + status);
        }
    }

    public static CpfNotificationProviderStatus up() {
        return new CpfNotificationProviderStatus("UP", "AVAILABLE");
    }

    public static CpfNotificationProviderStatus down(String reasonCode) {
        return new CpfNotificationProviderStatus("DOWN", reasonCode);
    }

    public static CpfNotificationProviderStatus unknown(String reasonCode) {
        return new CpfNotificationProviderStatus("UNKNOWN", reasonCode);
    }

    private static String require(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
