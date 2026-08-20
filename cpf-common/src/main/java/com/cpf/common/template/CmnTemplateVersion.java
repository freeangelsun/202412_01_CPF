package com.cpf.common.template;

import java.util.Objects;

/** Template definition plus persisted lifecycle and optimistic-lock revision. */
public record CmnTemplateVersion(
        CmnTemplateDefinition definition,
        CmnTemplateLifecycleStatus status,
        long revision) {
    public CmnTemplateVersion {
        definition = Objects.requireNonNull(definition, "definition");
        status = Objects.requireNonNull(status, "status");
        if (revision < 0) {
            throw new IllegalArgumentException("revision must not be negative");
        }
    }
}
