package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;
import java.util.Map;

/**
 * Runtime Agent의 자기 등록/재등록 계약입니다.
 *
 * <p>instanceId는 process/runtime identity이고 managedServerId는 stable managed server identity입니다.
 * systemCode는 Generated Domain/Runtime system identity이므로 둘을 서로 대체하지 않습니다.</p>
 */
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
        String managedServerId,
        String managementIdentity,
        String runtimeHostname,
        String systemCode,
        String applicationName,
        String applicationRole,
        Long processId,
        String javaVersion,
        String cpfVersion,
        String applicationVersion,
        Instant startedAt,
        Instant agentTime,
        int leaseSeconds) {

    public CpfRuntimeInstanceRegistration {
        capabilities = capabilities == null ? Map.of() : Map.copyOf(capabilities);
        labels = labels == null ? Map.of() : Map.copyOf(labels);
        startedAt = startedAt == null ? Instant.now() : startedAt;
        agentTime = agentTime == null ? Instant.now() : agentTime;
        leaseSeconds = Math.max(10, Math.min(3600, leaseSeconds <= 0 ? 60 : leaseSeconds));
    }

    /** 기존 Runtime Agent 생성 코드 호환입니다. */
    public CpfRuntimeInstanceRegistration(String instanceId, String serviceId, String endpointCode,
                                          String environment, String zone, String cell, String baseUrl,
                                          String artifactVersion, String artifactCommit, String runtimeRole,
                                          String registrationSource, String schemaVersion, String configHash,
                                          Map<String, String> capabilities, Map<String, String> labels,
                                          Instant agentTime, int leaseSeconds) {
        this(instanceId, serviceId, endpointCode, environment, zone, cell, baseUrl,
                artifactVersion, artifactCommit, runtimeRole, registrationSource, schemaVersion, configHash,
                capabilities, labels, null, null, null, null, null, runtimeRole, null, null,
                artifactVersion, artifactVersion, agentTime, agentTime, leaseSeconds);
    }

    /** 더 오래된 13-argument 생성 코드 호환입니다. */
    public CpfRuntimeInstanceRegistration(String instanceId, String serviceId, String endpointCode,
                                          String environment, String zone, String cell, String baseUrl,
                                          String artifactVersion, String schemaVersion, String configHash,
                                          Map<String, String> capabilities, Map<String, String> labels,
                                          int leaseSeconds) {
        this(instanceId, serviceId, endpointCode, environment, zone, cell, baseUrl,
                artifactVersion, "unknown", "APPLICATION", "SELF", schemaVersion, configHash,
                capabilities, labels, null, null, null, null, null, "APPLICATION", null, null,
                artifactVersion, artifactVersion, Instant.now(), Instant.now(), leaseSeconds);
    }
}
