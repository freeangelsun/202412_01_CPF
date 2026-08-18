package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;
import java.util.Map;

/** Stable server와 ephemeral Runtime/Capability를 함께 보여주는 중앙 Runtime Inventory projection입니다. */
public record CpfRuntimeInventorySnapshot(
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
        Map<String,String> capabilities,
        Instant startedAt,
        Instant lastSeenAt) {
    public CpfRuntimeInventorySnapshot {
        capabilities = capabilities == null ? Map.of() : Map.copyOf(capabilities);
    }
}
