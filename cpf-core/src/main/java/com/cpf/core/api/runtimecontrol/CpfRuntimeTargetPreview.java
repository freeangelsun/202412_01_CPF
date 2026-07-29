package com.cpf.core.api.runtimecontrol;

import java.util.List;

/** Runtime 변경 대상 Preview 결과입니다. */
public record CpfRuntimeTargetPreview(
        String changeType,
        int payloadSchemaVersion,
        boolean overbroad,
        int candidateCount,
        int eligibleCount,
        List<CpfRuntimeTargetPreviewItem> targets) {
    public CpfRuntimeTargetPreview {
        targets = targets == null ? List.of() : List.copyOf(targets);
    }
}
