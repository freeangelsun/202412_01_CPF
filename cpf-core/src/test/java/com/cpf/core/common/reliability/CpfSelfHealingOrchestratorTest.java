package com.cpf.core.common.reliability;

import com.cpf.core.api.reliability.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CpfSelfHealingOrchestratorTest {
    @Test
    void deniesNonAllowlistedAndRequiresApproval() {
        CpfSelfHealingGuard guard = new CpfSelfHealingGuard(new CpfSelfHealingPolicy(2, 2, Duration.ofMinutes(5), Duration.ZERO));
        CpfSelfHealingOrchestrator orchestrator = new CpfSelfHealingOrchestrator(
                guard,
                command -> CpfSelfHealingActionPort.ActionResult.succeeded("ok"),
                CpfSelfHealingEventSink.noop(),
                Set.of("DRAIN_INSTANCE"),
                Set.of("DRAIN_INSTANCE"));

        assertEquals("DENIED", orchestrator.attempt(
                new CpfSelfHealingOrchestrator.Request("svc-1", "RESTART", "health", null, null), Instant.now()).state());
        assertEquals("DENIED", orchestrator.attempt(
                new CpfSelfHealingOrchestrator.Request("svc-1", "DRAIN_INSTANCE", "health", null, null), Instant.now()).state());
        assertEquals("SUCCEEDED", orchestrator.attempt(
                new CpfSelfHealingOrchestrator.Request("svc-1", "DRAIN_INSTANCE", "health", "APR-1", null), Instant.now()).state());
    }
}
