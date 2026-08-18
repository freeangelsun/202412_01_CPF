package com.cpf.platform.operations.runtimecontrol;

/** ADM이 관리하는 Stable Managed Server master 변경 계약입니다. */
public record CpfManagedServerCommand(
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
        String reason,
        String operatorId) {
}
