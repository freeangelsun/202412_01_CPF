package com.cpf.platform.operations.runtimecontrol.api;

import java.time.Instant;

/**
 * 중앙 Runtime Registry가 소유하는 Runtime lifecycle snapshot입니다.
 *
 * <p>Batch 등 개별 Runtime은 이 값을 복제해 별도 master authority를 만들지 않습니다.</p>
 */
public record CpfManagedRuntimeSnapshot(
        String instanceId,
        String serviceId,
        String runtimeRole,
        String desiredState,
        String actualState,
        long controlVersion,
        long fencingToken,
        Instant leaseUntil,
        Instant lastHeartbeatAt,
        String environment,
        String zone,
        String cell,
        String artifactVersion) {
}
