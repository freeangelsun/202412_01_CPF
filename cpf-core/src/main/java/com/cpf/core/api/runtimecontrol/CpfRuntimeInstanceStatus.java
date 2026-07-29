package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Runtime instance의 lease·artifact·desired/actual 상태입니다. */
public record CpfRuntimeInstanceStatus(
        String instanceId,
        String serviceId,
        String environment,
        String zone,
        String cell,
        long fencingToken,
        Instant leaseUntil,
        long desiredVersion,
        long actualVersion,
        String desiredHash,
        String actualHash,
        String driftState,
        boolean maintenance,
        boolean draining,
        Instant drainDeadlineAt,
        Instant heartbeatAt,
        String artifactVersion,
        String artifactCommit,
        String runtimeRole,
        String registrationSource,
        long clockSkewMillis) {
}
