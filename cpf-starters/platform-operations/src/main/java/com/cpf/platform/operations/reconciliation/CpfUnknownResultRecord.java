package com.cpf.platform.operations.reconciliation;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/** Common durable record for an outbound, broker, file or batch result that cannot yet be finalized. */
public record CpfUnknownResultRecord(
        String unknownId,
        String unknownType,
        String unknownStatus,
        String transactionId,
        String segmentId,
        String externalKey,
        String failureCode,
        String failureMessage,
        String nextAction,
        Instant detectedAt,
        Instant resolvedAt) {

    public CpfUnknownResultRecord {
        unknownType = required(unknownType, "unknownType").toUpperCase(Locale.ROOT);
        unknownStatus = unknownStatus == null || unknownStatus.isBlank()
                ? "CHECK_PENDING"
                : required(unknownStatus, "unknownStatus").toUpperCase(Locale.ROOT);
        detectedAt = Objects.requireNonNull(detectedAt,
                "detectedAt is required; use CpfUnknownResultRecord.detectedNow(..., Clock) at the boundary");
        if (resolvedAt != null && resolvedAt.isBefore(detectedAt)) {
            throw new IllegalArgumentException("resolvedAt cannot precede detectedAt");
        }
    }

    public static CpfUnknownResultRecord detectedNow(
            String unknownId, String unknownType, String unknownStatus,
            String transactionId, String segmentId, String externalKey,
            String failureCode, String failureMessage, String nextAction, Clock clock) {
        return new CpfUnknownResultRecord(unknownId, unknownType, unknownStatus, transactionId,
                segmentId, externalKey, failureCode, failureMessage, nextAction,
                Objects.requireNonNull(clock, "clock").instant(), null);
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        String normalized = value.trim();
        if (normalized.length() > 128) throw new IllegalArgumentException(name + " exceeds 128 characters");
        return normalized;
    }
}
