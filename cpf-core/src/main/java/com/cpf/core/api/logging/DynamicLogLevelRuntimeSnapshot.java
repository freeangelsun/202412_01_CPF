package com.cpf.core.api.logging;

import java.time.LocalDateTime;
import java.util.List;

/** Immutable registry, version and bounded audit snapshot. */
public record DynamicLogLevelRuntimeSnapshot(
        long version,
        LocalDateTime observedAt,
        List<DynamicLogLevelRule> activeRules,
        List<DynamicLogLevelAuditRecord> auditRecords,
        int maximumAuditRecords,
        long droppedAuditRecordCount) {
    public DynamicLogLevelRuntimeSnapshot {
        activeRules = activeRules == null ? List.of() : List.copyOf(activeRules);
        auditRecords = auditRecords == null ? List.of() : List.copyOf(auditRecords);
        if (maximumAuditRecords < auditRecords.size()) {
            throw new IllegalArgumentException("maximumAuditRecords cannot be smaller than auditRecords");
        }
        if (droppedAuditRecordCount < 0L) {
            throw new IllegalArgumentException("droppedAuditRecordCount must be non-negative");
        }
    }

    /** Source-compatible constructor for consumers compiled against the original snapshot. */
    public DynamicLogLevelRuntimeSnapshot(
            long version,
            LocalDateTime observedAt,
            List<DynamicLogLevelRule> activeRules,
            List<DynamicLogLevelAuditRecord> auditRecords) {
        this(version, observedAt, activeRules, auditRecords,
                auditRecords == null ? 0 : auditRecords.size(), 0L);
    }
}
