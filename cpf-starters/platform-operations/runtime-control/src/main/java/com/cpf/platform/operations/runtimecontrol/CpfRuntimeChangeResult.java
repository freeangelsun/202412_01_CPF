package com.cpf.platform.operations.runtimecontrol;

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

    public CpfRuntimeChangeResult {
        state = CpfRuntimeChangeState.require(state).name();
        if (targetCount < 0 || acknowledgedCount < 0 || failedCount < 0 || driftCount < 0) {
            throw new IllegalArgumentException("Runtime change 집계 값은 음수일 수 없습니다.");
        }
        if (acknowledgedCount > targetCount || failedCount > targetCount || driftCount > targetCount) {
            throw new IllegalArgumentException("Runtime change 집계 값은 대상 수를 초과할 수 없습니다.");
        }
        if ((long) acknowledgedCount + failedCount > targetCount) {
            throw new IllegalArgumentException("Runtime change ACK와 실패 합계는 대상 수를 초과할 수 없습니다.");
        }
    }
}
