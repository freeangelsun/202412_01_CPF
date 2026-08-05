package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Runtime Agent 적용 결과 ACK입니다. 오래된 fencing token/out-of-order ACK는 Control Plane이 거부합니다. */
public record CpfRuntimeAck(
        String deliveryId,
        String changeId,
        String instanceId,
        long fencingToken,
        int attempt,
        long appliedVersion,
        String actualHash,
        String state,
        String errorCode,
        String message,
        Instant acknowledgedAt) {

    public CpfRuntimeAck {
        state = CpfRuntimeAckState.require(state).name();
        if (fencingToken < 0 || attempt < 0 || appliedVersion < 0) {
            throw new IllegalArgumentException(
                    "Runtime ACK fencingToken/attempt/appliedVersion은 음수일 수 없습니다.");
        }
    }

    /** 구버전 Agent의 첫 claim ACK 호환입니다. retry ACK는 attempt가 반드시 필요합니다. */
    public CpfRuntimeAck(
            String deliveryId,
            String changeId,
            String instanceId,
            long fencingToken,
            long appliedVersion,
            String actualHash,
            String state,
            String errorCode,
            String message,
            Instant acknowledgedAt) {
        this(deliveryId, changeId, instanceId, fencingToken, 0, appliedVersion, actualHash, state,
                errorCode, message, acknowledgedAt);
    }
}
