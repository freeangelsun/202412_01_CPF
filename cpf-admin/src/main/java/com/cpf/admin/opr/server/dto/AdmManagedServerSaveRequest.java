package com.cpf.admin.opr.server.dto;

public record AdmManagedServerSaveRequest(
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
        String tagsJson,
        Long expectedVersion,
        String reason) {
}
