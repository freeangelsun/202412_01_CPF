package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Runtime Agent 적용 결과 ACK입니다. 오래된 fencing token/out-of-order ACK는 Control Plane이 거부합니다. */
public record CpfRuntimeAck(
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

    public CpfRuntimeAck {
        state = CpfRuntimeAckState.require(state).name();
        if (fencingToken < 0 || appliedVersion < 0) {
            throw new IllegalArgumentException("Runtime ACK fencingToken/appliedVersion은 음수일 수 없습니다.");
        }
    }
}
