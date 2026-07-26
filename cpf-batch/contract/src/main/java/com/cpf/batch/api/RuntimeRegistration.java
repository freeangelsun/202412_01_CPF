package com.cpf.batch.api;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Runtime 자체가 보고하는 불변 Identity/Version 등록 계약. */
public record RuntimeRegistration(
        RuntimeRole runtimeRole,
        String serviceId,
        String instanceId,
        String moduleId,
        String wasId,
        String hostAlias,
        String zone,
        String pool,
        String artifactVersion,
        String gitSha,
        String checksum,
        String profile,
        List<String> capabilities,
        Map<String, String> endpoints,
        String configVersion,
        String schemaCompatibility,
        String commandApiVersion,
        Instant startedAt
) {
    public RuntimeRegistration {
        Objects.requireNonNull(runtimeRole, "runtimeRole");
        require(serviceId, "serviceId");
        require(instanceId, "instanceId");
        require(artifactVersion, "artifactVersion");
        capabilities = capabilities == null ? List.of() : List.copyOf(capabilities);
        endpoints = endpoints == null ? Map.of() : Map.copyOf(endpoints);
        startedAt = startedAt == null ? Instant.now() : startedAt;
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}
