package com.cpf.platform.operations.runtimecontrol;

import java.time.Instant;
import java.util.List;

/** Runtime Control Plane 운영 Health/Readiness/SLO snapshot입니다. */
public record CpfRuntimeControlHealth(
        boolean ready,
        String status,
        String controllerId,
        long controllerFencingToken,
        int instanceCount,
        int backlogCount,
        int poisonedCount,
        int unknownResultCount,
        int driftCount,
        int expiredLeaseCount,
        long oldestDeliveryLagSeconds,
        boolean deliveryLagSloExceeded,
        List<String> reasonCodes,
        Instant observedAt) {
    public CpfRuntimeControlHealth {
        reasonCodes = reasonCodes == null ? List.of() : List.copyOf(reasonCodes);
        observedAt = observedAt == null ? Instant.now() : observedAt;
    }
}
