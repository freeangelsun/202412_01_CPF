package com.cpf.gateway.runtime;

import com.cpf.core.api.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.core.api.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfGatewayRuntimeApplierTest {
    @Test
    void rejectsIntegerOverflowInsteadOfApplyingWrappedRateLimit() {
        var applier = new CpfGatewayRuntimeApplier("RATE_LIMIT", new CpfGatewayRuntimePolicy());
        var result = applier.apply(delivery(
                CpfRuntimePayload.parse("{\"permits\":4294967297,\"windowMillis\":60000}")));

        assertFalse(result.applied());
        assertEquals("RATE_LIMIT_INVALID", result.errorCode());
    }

    @Test
    void appliesValidRateLimitPayload() {
        var applier = new CpfGatewayRuntimeApplier("RATE_LIMIT", new CpfGatewayRuntimePolicy());
        var result = applier.apply(delivery(
                CpfRuntimePayload.parse("{\"permits\":10,\"windowMillis\":60000}")));

        assertTrue(result.applied());
        assertEquals("payload-hash", result.actualHash());
    }

    private static CpfRuntimeDelivery delivery(CpfRuntimePayload payload) {
        return new CpfRuntimeDelivery(
                "delivery", "change", "RATE_LIMIT", "gateway-1",
                1L, 1L, "request-hash", "payload-hash", payload, 1,
                Instant.parse("2026-08-05T01:00:00Z"));
    }
}
