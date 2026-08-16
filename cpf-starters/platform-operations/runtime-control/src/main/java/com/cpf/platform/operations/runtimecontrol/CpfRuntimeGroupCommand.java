package com.cpf.platform.operations.runtimecontrol;

/** Runtime Instance Group/Cell/Zone 생성·수정 명령입니다. */
public record CpfRuntimeGroupCommand(
        String operationId,
        String groupId,
        String groupName,
        String parentGroupId,
        String environment,
        String description,
        Long expectedVersion,
        boolean active,
        String reason,
        String requestedBy) {}
