package com.cpf.platform.operations.runtimecontrol;

import java.util.List;

/** Runtime Group 현재 상태입니다. */
public record CpfRuntimeGroupResult(
        String groupId,
        String groupName,
        String parentGroupId,
        String environment,
        String description,
        boolean active,
        long rowVersion,
        List<String> instanceIds) {
    public CpfRuntimeGroupResult { instanceIds = instanceIds == null ? List.of() : List.copyOf(instanceIds); }
}
