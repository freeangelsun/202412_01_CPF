package com.cpf.messaging.schema.api;

import java.util.List;

/** 스키마 호환성 판정과 위반 사유. */
public record CpfEventSchemaCompatibilityResult(boolean compatible, List<String> violations) {
    public CpfEventSchemaCompatibilityResult {
        violations = violations == null ? List.of() : List.copyOf(violations);
        if (compatible && !violations.isEmpty()) {
            throw new IllegalArgumentException("compatible result cannot contain violations");
        }
    }
}
