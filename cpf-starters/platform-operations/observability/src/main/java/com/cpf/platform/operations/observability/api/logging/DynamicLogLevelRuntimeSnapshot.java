package com.cpf.platform.operations.observability.api.logging;

import java.time.LocalDateTime;
import java.util.List;

/** Immutable registry, version and bounded audit snapshot. */
/** DynamicLogLevelRuntimeSnapshot 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
    /** DynamicLogLevelRuntimeSnapshot 작업을 CPF 표준 계약에 따라 수행한다. */
    public DynamicLogLevelRuntimeSnapshot(
            long version,
            LocalDateTime observedAt,
            List<DynamicLogLevelRule> activeRules,
            List<DynamicLogLevelAuditRecord> auditRecords) {
        this(version, observedAt, activeRules, auditRecords,
                auditRecords == null ? 0 : auditRecords.size(), 0L);
    }
}
