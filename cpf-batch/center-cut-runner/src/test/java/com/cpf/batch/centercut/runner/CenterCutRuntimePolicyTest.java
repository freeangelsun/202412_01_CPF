package com.cpf.batch.centercut.runner;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CenterCutRuntimePolicyTest {
    @Test
    void sharedPolicyControlsActualCenterCutPollGate() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        try (CenterCutRuntime runtime = new CenterCutRuntime(null, null, "R1", "P1", 30)) {
            runtime.setRuntimePolicy(policy);
            assertTrue(runtime.runtimeEnabled());
            policy.replaceCenterCut(1L, false);
            assertFalse(runtime.runtimeEnabled());
            assertTrue(runtime.draining());
        }
    }
}
