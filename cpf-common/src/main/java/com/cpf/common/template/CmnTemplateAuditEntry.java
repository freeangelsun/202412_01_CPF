package com.cpf.common.template;

import java.time.Instant;
import java.util.Objects;

/** Append-only audit event for common-template lifecycle changes. */
public record CmnTemplateAuditEntry(
        String auditId, String templateCode, long version, String channel, String action,
        String actor, String reason, String beforeStatus, String afterStatus, long revision, Instant occurredAt) {
    public CmnTemplateAuditEntry {
        auditId = required(auditId, "auditId");
        templateCode = required(templateCode, "templateCode");
        if (version <= 0) throw new IllegalArgumentException("version must be greater than zero");
        channel = required(channel, "channel");
        action = required(action, "action");
        actor = required(actor, "actor");
        reason = required(reason, "reason");
        afterStatus = required(afterStatus, "afterStatus");
        if (revision < 0) throw new IllegalArgumentException("revision must not be negative");
        occurredAt = Objects.requireNonNull(occurredAt, "occurredAt");
    }
    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}
