package com.cpf.admin.opr.server.dto;

import com.cpf.platform.operations.runtimecontrol.CpfManagedServerSnapshot;

import java.time.Instant;

public record AdmManagedServerResponse(
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
    public static AdmManagedServerResponse from(CpfManagedServerSnapshot source) {
        return new AdmManagedServerResponse(source.managedServerId(), source.serverName(), source.displayName(),
                source.hostname(), source.managementIdentity(), source.environment(), source.serverGroup(), source.zone(),
                source.location(), source.description(), source.status(), source.enabled(), source.tagsJson(),
                source.rowVersion(), source.runtimeCount(), source.activeRuntimeCount(), source.registeredAt(), source.updatedAt());
    }
}
