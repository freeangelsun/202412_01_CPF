package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Runtime Control leader lease 상태입니다. */
public record CpfRuntimeControllerStatus(
        String holderId,
        long fencingToken,
        Instant leaseUntil,
        Instant lastReconciledAt) {
}
