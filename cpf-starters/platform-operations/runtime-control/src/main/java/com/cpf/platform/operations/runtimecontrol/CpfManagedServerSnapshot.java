package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;

/** Central Managed Server Registry 조회 모델입니다. */
public record CpfManagedServerSnapshot(
        String managedServerId,
        String serverName,
        String displayName,
        String hostname,
        String managementIdentity,
        String environment,
        String serverGroup,
        String zone,
        String location,
        String description,
        String status,
        boolean enabled,
        String tagsJson,
        long rowVersion,
        long runtimeCount,
        long activeRuntimeCount,
        Instant registeredAt,
        Instant updatedAt) {
}
