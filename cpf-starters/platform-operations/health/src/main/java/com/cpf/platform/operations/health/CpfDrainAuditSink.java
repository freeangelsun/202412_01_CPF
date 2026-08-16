package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.CpfDrainState;
import java.time.Instant;

@FunctionalInterface
public interface CpfDrainAuditSink {
    void record(String action, String reason, CpfDrainState result, long inFlight, Instant occurredAt);
}
