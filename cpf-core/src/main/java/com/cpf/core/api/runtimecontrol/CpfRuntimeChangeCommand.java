package com.cpf.core.api.runtimecontrol;

import java.time.Instant;
import java.util.Map;

/** Runtime 변경 생성 명령입니다. operationId/requestHash/expectedVersion으로 멱등성과 CAS를 보장합니다. */
public record CpfRuntimeChangeCommand(
        String operationId,
        String changeType,
        int payloadSchemaVersion,
        CpfRuntimeTargetSelector target,
        Map<String, Object> payload,
        Long expectedVersion,
        String rolloutMode,
        Integer waveSize,
        Integer quorumPercent,
        Instant scheduledAt,
        Instant expiresAt,
        String reason,
        String approvalId,
        String breakGlassId,
        String requestedBy) {

    public CpfRuntimeChangeCommand {
        payloadSchemaVersion = payloadSchemaVersion <= 0 ? 1 : payloadSchemaVersion;
        payload = payload == null ? Map.of() : Map.copyOf(payload);
        rolloutMode = rolloutMode == null || rolloutMode.isBlank() ? "ALL_AT_ONCE" : rolloutMode.trim().toUpperCase();
        waveSize = waveSize == null || waveSize < 1 ? 1 : waveSize;
        quorumPercent = quorumPercent == null ? 100 : Math.max(1, Math.min(100, quorumPercent));
    }

    /** 기존 14-인자 생성 코드 호환입니다. */
    public CpfRuntimeChangeCommand(String operationId, String changeType, CpfRuntimeTargetSelector target,
                                   Map<String, Object> payload, Long expectedVersion, String rolloutMode,
                                   Integer waveSize, Integer quorumPercent, Instant scheduledAt,
                                   Instant expiresAt, String reason, String approvalId,
                                   String breakGlassId, String requestedBy) {
        this(operationId, changeType, 1, target, payload, expectedVersion, rolloutMode, waveSize,
                quorumPercent, scheduledAt, expiresAt, reason, approvalId, breakGlassId, requestedBy);
    }
}
