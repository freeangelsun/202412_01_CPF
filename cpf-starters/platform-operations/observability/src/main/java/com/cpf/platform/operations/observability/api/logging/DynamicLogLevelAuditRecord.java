package com.cpf.platform.operations.observability.api.logging;

import java.time.LocalDateTime;

/** atomically 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
