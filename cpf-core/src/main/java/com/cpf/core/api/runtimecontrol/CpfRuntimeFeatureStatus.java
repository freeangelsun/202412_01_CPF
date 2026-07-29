package com.cpf.core.api.runtimecontrol;

import java.time.Instant;

/** Instance별 Runtime capability desired/actual 상태입니다. */
public record CpfRuntimeFeatureStatus(
        String instanceId,
        String serviceId,
        String changeType,
        long desiredVersion,
        long actualVersion,
        String desiredHash,
        String actualHash,
        String driftState,
        String sourceDeliveryId,
        Instant updatedAt) {
}
