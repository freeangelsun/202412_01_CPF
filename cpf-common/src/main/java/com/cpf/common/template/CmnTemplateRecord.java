package com.cpf.common.template;

import java.time.Instant;
import java.util.Objects;

/**
 * @deprecated Compatibility result of the disabled {@link CmnTemplateRepository}. Product
 * consumers must use {@link CmnTemplateVersion} and {@link CmnTemplateAuditEntry}.
 */
@Deprecated(forRemoval = true)
public record CmnTemplateRecord(
        CmnTemplateDefinition definition,
        Status status,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String updatedBy,
        Instant updatedAt) {
    /** Template lifecycle의 사용 가능 상태를 나타내는 Canonical 상태값입니다. */
    @Deprecated
    public enum Status { DRAFT, APPROVED, RETIRED }

    @Deprecated
    public CmnTemplateRecord {
        definition = Objects.requireNonNull(definition, "definition");
        status = Objects.requireNonNull(status, "status");
        createdBy = required(createdBy, "createdBy");
        createdAt = Objects.requireNonNull(createdAt, "createdAt");
        updatedBy = required(updatedBy, "updatedBy");
        updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        if (status == Status.APPROVED && (approvedBy == null || approvedAt == null)) {
            throw new IllegalArgumentException("approved template requires approver and approvedAt");
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value.trim();
    }
}
