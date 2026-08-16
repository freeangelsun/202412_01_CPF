package com.cpf.local.batch;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Local Batch Runtime이 Canonical 역할명과 Property 경로만 노출하는지 검증합니다. */
class CpfLocalBatchRuntimeStatusControllerTest {
    @Test
    void exposesCanonicalBatchRoles() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.local.batch.modules.control-plane", "false")
                .withProperty("cpf.local.batch.ports.agent", "19094");

        CpfLocalBatchRuntimeStatusController.Status status =
                new CpfLocalBatchRuntimeStatusController(environment).status();

        assertEquals(
                List.of("control-plane", "scheduler", "worker", "center-cut", "agent"),
                status.roles().stream().map(CpfLocalBatchRuntimeStatusController.Role::name).toList());
        assertFalse(status.roles().getFirst().enabled());
        assertEquals(19094, status.roles().getLast().port());
    }
}
