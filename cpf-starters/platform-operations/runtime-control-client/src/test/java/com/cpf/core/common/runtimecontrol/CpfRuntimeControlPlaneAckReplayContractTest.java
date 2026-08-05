package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeControlPlaneAckReplayContractTest {
    @Test
    void terminalReplayRequiresStateErrorAndActualHashIdentity() {
        assertTrue(CpfRuntimeControlPlaneRepository.isIdempotentTerminalAck(
                "ACKED", "SUCCESS", "hash", "hash", null, null));
        assertFalse(CpfRuntimeControlPlaneRepository.isIdempotentTerminalAck(
                "ACKED", "SUCCESS", "hash-a", "hash-b", null, null));
        assertTrue(CpfRuntimeControlPlaneRepository.isIdempotentTerminalAck(
                "UNKNOWN_RESULT", "UNKNOWN_RESULT", "evidence", "evidence", "TIMEOUT", "TIMEOUT"));
        assertFalse(CpfRuntimeControlPlaneRepository.isIdempotentTerminalAck(
                "UNKNOWN_RESULT", "UNKNOWN_RESULT", "evidence-a", "evidence-b", "TIMEOUT", "TIMEOUT"));
        assertFalse(CpfRuntimeControlPlaneRepository.isIdempotentTerminalAck(
                "RESTART_REQUIRED", "RESTART_REQUIRED", "stage-a", "stage-b",
                "RESTART_REQUIRED", "RESTART_REQUIRED"));
    }
}
