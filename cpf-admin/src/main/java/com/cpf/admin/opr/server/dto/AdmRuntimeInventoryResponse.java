package com.cpf.admin.opr.server.dto;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeInventorySnapshot;

import java.time.Instant;
import java.util.Map;

public record AdmRuntimeInventoryResponse(
        String instanceId,
        String managedServerId,
        String serverName,
        String serviceId,
        String systemCode,
        String applicationName,
        String applicationRole,
        String runtimeHostname,
        String environment,
        String zone,
        String status,
        String artifactVersion,
        String cpfVersion,
        String javaVersion,
        Map<String, String> capabilities,
        Instant startedAt,
        Instant lastSeenAt) {
    public AdmRuntimeInventoryResponse {
        capabilities = capabilities == null ? Map.of() : Map.copyOf(capabilities);
    }

    public static AdmRuntimeInventoryResponse from(CpfRuntimeInventorySnapshot source) {
        return new AdmRuntimeInventoryResponse(source.instanceId(), source.managedServerId(), source.serverName(),
                source.serviceId(), source.systemCode(), source.applicationName(), source.applicationRole(),
                source.runtimeHostname(), source.environment(), source.zone(), source.status(), source.artifactVersion(),
                source.cpfVersion(), source.javaVersion(), source.capabilities(), source.startedAt(), source.lastSeenAt());
    }
}
