package com.cpf.platform.operations.api.health;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** 시스템/인스턴스 단위 런타임 Health 스냅샷입니다. */
public record CpfRuntimeHealth(String systemId, String instanceId, CpfHealthStatus liveness,
                               CpfHealthStatus readiness, CpfHealthStatus startup,
                               boolean draining, boolean maintenance, String version,
                               String buildSha, Instant startedAt, Instant observedAt,
                               long uptimeMillis, List<String> warnings,
                               List<String> capabilities, List<CpfDependencyHealth> dependencies,
                               Map<String, String> details) {
    public CpfRuntimeHealth {
        if (systemId == null || systemId.isBlank()) throw new IllegalArgumentException("systemId required");
        if (instanceId == null || instanceId.isBlank()) throw new IllegalArgumentException("instanceId required");
        liveness = liveness == null ? CpfHealthStatus.UNKNOWN : liveness;
        readiness = readiness == null ? CpfHealthStatus.UNKNOWN : readiness;
        startup = startup == null ? CpfHealthStatus.UNKNOWN : startup;
        version = version == null ? "unknown" : version;
        buildSha = buildSha == null ? "unknown" : buildSha;
        startedAt = startedAt == null ? Instant.EPOCH : startedAt;
        observedAt = observedAt == null ? Instant.EPOCH : observedAt;
        if (uptimeMillis < 0) uptimeMillis = 0;
        warnings = warnings == null ? List.of() : List.copyOf(warnings);
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        dependencies = dependencies == null ? List.of() : List.copyOf(dependencies);
        details = details == null ? Map.of() : Map.copyOf(details);
    }
    /** instanceKey 작업을 CPF 표준 계약에 따라 수행한다. */
    public String instanceKey() { return systemId + ":" + instanceId; }
}
