package com.cpf.platform.operations.api.health;

import java.time.Instant;
import java.util.Map;

/** 외부 의존성 상태. endpoint는 원문 대신 마스킹된 식별자만 허용합니다. */
public record CpfDependencyHealth(String name, String endpointRef, CpfHealthStatus status,
                                  String reasonCode, Instant checkedAt, long latencyMillis,
                                  Map<String, String> details) {
    public CpfDependencyHealth {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
        if (status == null) status = CpfHealthStatus.UNKNOWN;
        if (checkedAt == null) checkedAt = Instant.EPOCH;
        if (latencyMillis < 0) latencyMillis = 0;
        endpointRef = endpointRef == null || endpointRef.isBlank() ? "masked" : endpointRef;
        reasonCode = reasonCode == null ? "" : reasonCode;
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
