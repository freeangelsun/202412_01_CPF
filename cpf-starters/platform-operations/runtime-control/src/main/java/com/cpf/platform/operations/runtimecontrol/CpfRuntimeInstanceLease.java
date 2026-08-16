package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;

/** 등록된 Runtime Instance의 lease/fencing 결과입니다. */
public record CpfRuntimeInstanceLease(
        String instanceId,
        long fencingToken,
        long desiredVersion,
        long actualVersion,
        String desiredHash,
        String actualHash,
        String driftState,
        Instant leaseUntil) {
}
