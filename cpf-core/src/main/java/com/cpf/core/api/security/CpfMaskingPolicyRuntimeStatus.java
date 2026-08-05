package com.cpf.core.api.security;

import java.time.Instant;

/** Bounded operational snapshot that never exposes sensitive keys or reasons. */
public record CpfMaskingPolicyRuntimeStatus(
        Health health,
        long activeVersion,
        int historySize,
        int commandRecordCount,
        int maximumHistory,
        int maximumCommandRecords,
        long rejectedCommandCount,
        long auditFailureCount,
        Instant observedAt) {
    public CpfMaskingPolicyRuntimeStatus {
        if (health == null || observedAt == null) throw new IllegalArgumentException("status fields are required");
        if (activeVersion < 1L || historySize < 0 || commandRecordCount < 0
                || maximumHistory < 1 || maximumCommandRecords < 1
                || rejectedCommandCount < 0L || auditFailureCount < 0L) {
            throw new IllegalArgumentException("invalid masking policy runtime status");
        }
    }

    public enum Health { UP, DEGRADED, DOWN }
}
