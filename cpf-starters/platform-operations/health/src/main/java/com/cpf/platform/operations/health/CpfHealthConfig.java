package com.cpf.platform.operations.health;
import java.time.Duration;
/** Spring과 무관하게 Runtime 테스트 가능한 Health 설정 Value입니다. */
public record CpfHealthConfig(Duration dependencyTimeout, Duration cacheTtl, int maxConcurrentChecks,
                              String systemId, String instanceId, String version, String buildSha,
                              boolean maintenance) {
    public CpfHealthConfig {
        if (dependencyTimeout == null || dependencyTimeout.isZero() || dependencyTimeout.isNegative()) throw new IllegalArgumentException("dependencyTimeout must be positive");
        if (cacheTtl == null || cacheTtl.isNegative()) throw new IllegalArgumentException("cacheTtl must not be negative");
        if (maxConcurrentChecks < 1) throw new IllegalArgumentException("maxConcurrentChecks must be >= 1");
        if (systemId == null || systemId.isBlank()) throw new IllegalArgumentException("systemId required");
        if (instanceId == null || instanceId.isBlank()) throw new IllegalArgumentException("instanceId required");
        version = version == null ? "unknown" : version;
        buildSha = buildSha == null ? "unknown" : buildSha;
    }
}
