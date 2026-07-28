package com.cpf.core.api.runtimecontrol;

/** Runtime Group membership 멱등 변경 명령입니다. */
public record CpfRuntimeGroupMemberCommand(
        String operationId,
        String groupId,
        String instanceId,
        boolean active,
        String reason,
        String requestedBy) {}
