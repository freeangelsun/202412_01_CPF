package com.cpf.platform.operations.runtimecontrol;

import java.util.List;
import java.util.Map;

/** Runtime Control Plane 변경의 대상 선택자입니다. */
public record CpfRuntimeTargetSelector(
        String environment,
        String serviceId,
        String groupId,
        List<String> instanceIds,
        List<String> excludeInstanceIds,
        Map<String, String> labels,
        String zone,
        String cell,
        boolean includeDraining,
        boolean includeMaintenance,
        boolean allowAll) {

    public CpfRuntimeTargetSelector {
        instanceIds = instanceIds == null ? List.of() : List.copyOf(instanceIds);
        excludeInstanceIds = excludeInstanceIds == null ? List.of() : List.copyOf(excludeInstanceIds);
        labels = labels == null ? Map.of() : Map.copyOf(labels);
    }

    /** 기존 9-인자 생성 코드 호환입니다. */
    public CpfRuntimeTargetSelector(String environment, String serviceId, String groupId,
                                    List<String> instanceIds, Map<String, String> labels,
                                    String zone, String cell, boolean includeDraining,
                                    boolean includeMaintenance) {
        this(environment, serviceId, groupId, instanceIds, List.of(), labels, zone, cell,
                includeDraining, includeMaintenance, false);
    }
}
