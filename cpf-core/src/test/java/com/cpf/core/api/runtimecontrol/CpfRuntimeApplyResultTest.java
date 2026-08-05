package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfRuntimeApplyResultTest {
    @Test
    void factoryMethodsProduceUnambiguousStates() {
        CpfRuntimeApplyResult success = CpfRuntimeApplyResult.success(" hash ");
        CpfRuntimeApplyResult unknown = CpfRuntimeApplyResult.unknown("UNKNOWN", " uncertain ");
        CpfRuntimeApplyResult restart = CpfRuntimeApplyResult.restartRequired("staged", "restart");

        assertTrue(success.applied());
        assertEquals("hash", success.actualHash());
        assertTrue(unknown.unknownResult());
        assertTrue(restart.restartRequired());
    }

    @Test
    void rejectsContradictorySignalsAndMissingAppliedHash() {
        assertThrows(IllegalArgumentException.class, () ->
                new CpfRuntimeApplyResult(true, true, false, "hash", null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new CpfRuntimeApplyResult(true, false, false, " ", null, null));
        assertThrows(IllegalArgumentException.class, () ->
                new CpfRuntimeApplyResult(false, false, true, null, "RESTART_REQUIRED", null));
        assertThrows(IllegalArgumentException.class, () ->
                CpfRuntimeApplyResult.success("x".repeat(65)));
        assertThrows(IllegalArgumentException.class, () ->
                CpfRuntimeApplyResult.restartRequired("x".repeat(65), "restart"));
    }
}
