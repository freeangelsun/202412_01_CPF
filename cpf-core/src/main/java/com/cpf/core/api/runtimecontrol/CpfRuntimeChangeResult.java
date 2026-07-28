package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Runtime 변경의 운영 조회 결과입니다. desired/actual/ACK/drift 상태를 한 번에 판단할 수 있습니다. */
public record CpfRuntimeChangeResult(
        String changeId,
        String operationId,
        String changeType,
        String state,
        long desiredVersion,
        String requestHash,
        int targetCount,
        int acknowledgedCount,
        int failedCount,
        int driftCount,
        Instant scheduledAt,
        Instant expiresAt,
        Instant createdAt,
        Instant updatedAt,
        String message) {
}
