package com.cpf.platform.operations.observability.internal.logging;

import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

/** Canonical idempotency identity shared by direct DB writes and durable log replay. */
public final class CpfTransactionLogIdentity {
    private CpfTransactionLogIdentity() {
    }

    /**
     * Returns the existing valid identity or assigns a new one.
     * Stable transaction context produces a deterministic identity; missing context uses a one-time random identity
     * to prevent unrelated malformed events from being falsely deduplicated.
     */
    public static String ensure(TransactionLogRecord record) {
        if (record == null) {
            throw new IllegalArgumentException("transaction log record is required");
        }
        String existing = normalizeExisting(record.getRecoveryEventId());
        if (existing != null) {
            return existing;
        }
        String transactionId = normalized(record.getTransactionId());
        String identity = transactionId == null
                ? sha256("MISSING_CONTEXT|" + UUID.randomUUID())
                : sha256(transactionId + '|'
                        + text(record.getSpanId(), "ROOT") + '|'
                        + text(record.getLogType(), "TRANSACTION_FINAL") + '|'
                        + (record.getSequenceNo() == null ? 1 : record.getSequenceNo()));
        record.setRecoveryEventId(identity);
        return identity;
    }

    /** Returns a deterministic non-reversible correlation value for operational logs. */
    public static String opaque(String transactionId) {
        String normalized = normalized(transactionId);
        return normalized == null ? "N/A" : "sha256:" + sha256(normalized);
    }

    public static boolean valid(String value) {
        if (value == null || value.length() != 64) {
            return false;
        }
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);
            if (!((ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f'))) {
                return false;
            }
        }
        return true;
    }

    private static String normalizeExisting(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!valid(normalized)) {
            throw new IllegalArgumentException("invalid transaction log recovery identity");
        }
        return normalized;
    }

    private static String normalized(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private static String text(String value, String fallback) {
        String normalized = normalized(value);
        return normalized == null ? fallback : normalized;
    }

    private static String sha256(String canonical) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
