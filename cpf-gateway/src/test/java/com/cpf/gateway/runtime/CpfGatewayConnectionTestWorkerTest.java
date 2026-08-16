package com.cpf.gateway.runtime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CpfGatewayConnectionTestWorkerTest {
    @Test
    void onlyApplicationLevelTestsCanChangeRoutingHealth() {
        assertFalse(CpfGatewayConnectionTestWorker.affectsRoutingHealth("NETWORK"));
        assertFalse(CpfGatewayConnectionTestWorker.affectsRoutingHealth("TCP"));
        assertFalse(CpfGatewayConnectionTestWorker.affectsRoutingHealth("TLS"));
        assertTrue(CpfGatewayConnectionTestWorker.affectsRoutingHealth("APPLICATION"));
        assertTrue(CpfGatewayConnectionTestWorker.affectsRoutingHealth("GATEWAY_E2E"));
    }
}
