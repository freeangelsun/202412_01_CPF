package com.cpf.core.common.runtime;

import java.time.Instant;
import java.util.Map;

/**
 * runtime health 관제 DTO입니다.
 */
public record CpfRuntimeHealthStatus(
        String componentId,
        String componentType,
        String status,
        Instant checkedAt,
        Map<String, String> attributes) {

    public CpfRuntimeHealthStatus {
        checkedAt = checkedAt == null ? Instant.now() : checkedAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
