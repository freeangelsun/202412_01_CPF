package com.cpf.starter.platform.operations.health;

import com.cpf.core.api.health.CpfDrainState;
import java.time.Instant;

@FunctionalInterface
public interface CpfDrainAuditSink {
    void record(String action, String reason, CpfDrainState result, long inFlight, Instant occurredAt);
}
