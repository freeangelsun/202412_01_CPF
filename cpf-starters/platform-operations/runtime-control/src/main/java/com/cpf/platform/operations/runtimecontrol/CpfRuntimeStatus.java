package com.cpf.platform.operations.runtimecontrol;

import java.util.List;

/** Runtime Control 운영 현황의 Typed Snapshot입니다. */
public record CpfRuntimeStatus(
        List<CpfRuntimeInstanceStatus> instances,
        List<CpfRuntimeFeatureStatus> featureStates,
        CpfRuntimeControllerStatus controller,
        List<CpfRuntimeDeliveryCount> deliveryCounts,
        int instanceCount,
        long driftCount,
        long expiredLeaseCount) {
    public CpfRuntimeStatus {
        instances = instances == null ? List.of() : List.copyOf(instances);
        featureStates = featureStates == null ? List.of() : List.copyOf(featureStates);
        deliveryCounts = deliveryCounts == null ? List.of() : List.copyOf(deliveryCounts);
    }
}
