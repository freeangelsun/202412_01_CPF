package com.cpf.core.api.logging;

import java.time.LocalDateTime;

/** Immutable local-runtime audit record atomically committed with a registry mutation. */
public record DynamicLogLevelAuditRecord(
        String auditId,
        String action,
        String targetRuleId,
        String actor,
        String reasonMasked,
        DynamicLogLevelRule beforeRule,
        DynamicLogLevelRule afterRule,
        long committedVersion,
        LocalDateTime occurredAt) {
}
