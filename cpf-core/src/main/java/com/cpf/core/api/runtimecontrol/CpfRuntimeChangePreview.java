package com.cpf.core.api.runtimecontrol;

import java.util.List;

/** Runtime 변경 적용 전 대상·Hash·Restart 영향 Preview입니다. */
public record CpfRuntimeChangePreview(
        CpfRuntimeTargetPreview targetPreview,
        String payloadHash,
        List<CpfRuntimeImpactCount> restartImpactSummary,
        List<CpfRuntimeInstanceDiff> instanceDiff,
        List<String> affectedServices) {
    public CpfRuntimeChangePreview {
        restartImpactSummary = restartImpactSummary == null ? List.of() : List.copyOf(restartImpactSummary);
        instanceDiff = instanceDiff == null ? List.of() : List.copyOf(instanceDiff);
        affectedServices = affectedServices == null ? List.of() : List.copyOf(affectedServices);
    }
}
