package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;
import java.util.Map;

/** Runtime Agent의 자기 등록/재등록 계약입니다. */
public record CpfRuntimeInstanceRegistration(
        String instanceId,
        String serviceId,
        String endpointCode,
        String environment,
        String zone,
        String cell,
        String baseUrl,
        String artifactVersion,
        String artifactCommit,
        String runtimeRole,
        String registrationSource,
        String schemaVersion,
        String configHash,
        Map<String, String> capabilities,
        Map<String, String> labels,
        Instant agentTime,
        int leaseSeconds) {

    public CpfRuntimeInstanceRegistration {
        capabilities = capabilities == null ? Map.of() : Map.copyOf(capabilities);
        labels = labels == null ? Map.of() : Map.copyOf(labels);
        agentTime = agentTime == null ? Instant.now() : agentTime;
        leaseSeconds = Math.max(10, Math.min(3600, leaseSeconds <= 0 ? 60 : leaseSeconds));
    }

    /** 기존 생성 코드 호환입니다. */
    public CpfRuntimeInstanceRegistration(String instanceId, String serviceId, String endpointCode,
                                          String environment, String zone, String cell, String baseUrl,
                                          String artifactVersion, String schemaVersion, String configHash,
                                          Map<String, String> capabilities, Map<String, String> labels,
                                          int leaseSeconds) {
        this(instanceId, serviceId, endpointCode, environment, zone, cell, baseUrl,
                artifactVersion, "unknown", "APPLICATION", "SELF", schemaVersion, configHash,
                capabilities, labels, Instant.now(), leaseSeconds);
    }
}
