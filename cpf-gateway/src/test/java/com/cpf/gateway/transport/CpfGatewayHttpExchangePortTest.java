package com.cpf.gateway.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CpfGatewayHttpExchangePortTest {
    @Test
    void responseTimeoutIsBoundedByOverallTimeout() {
        var policy = new CpfGatewayHttpExchangePort.TimeoutPolicy(500, 5_000, 2_000);
        assertEquals(2_000L, policy.effectiveResponseTimeoutMillis());
    }

    @Test
    void connectTimeoutCannotExceedOverallTimeout() {
        assertThrows(IllegalArgumentException.class,
                () -> new CpfGatewayHttpExchangePort.TimeoutPolicy(3_000, 2_000, 1_000));
    }
}
