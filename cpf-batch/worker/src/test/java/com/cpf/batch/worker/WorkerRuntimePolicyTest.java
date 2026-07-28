package com.cpf.batch.worker;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkerRuntimePolicyTest {
    @Test
    void sharedRuntimePolicyChangesActualPollCapacity() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        try (WorkerRuntime runtime = new WorkerRuntime(null, null, "W1", "1", "GENERAL", 8, 30)) {
            runtime.setRuntimePolicy(policy);
            assertEquals(8, runtime.availableCapacity());
            policy.replaceConcurrency(1L, true, 3);
            assertEquals(3, runtime.availableCapacity());
            policy.replaceConcurrency(2L, false, 3);
            assertEquals(0, runtime.availableCapacity());
        }
    }
}
