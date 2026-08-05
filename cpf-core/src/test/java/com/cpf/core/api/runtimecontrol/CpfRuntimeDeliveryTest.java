package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeDeliveryTest {
    @Test
    void acceptsCanonicalDeliveryAndDefaultsNullPayload() {
        CpfRuntimeDelivery delivery = delivery(1, 0L, 0L, 0);

        assertEquals(1, delivery.payloadSchemaVersion());
        assertEquals(CpfRuntimePayload.empty(), delivery.payload());
    }

    @Test
    void rejectsInvalidIdentityVersionFenceSchemaAndAttempt() {
        assertThrows(IllegalArgumentException.class, () -> new CpfRuntimeDelivery(
                " ", "change", "TYPE", "instance", 0L, 0L, "request", "payload",
                1, CpfRuntimePayload.empty(), 0, Instant.MAX));
        assertThrows(IllegalArgumentException.class, () -> delivery(1, -1L, 0L, 0));
        assertThrows(IllegalArgumentException.class, () -> delivery(1, 0L, -1L, 0));
        assertThrows(IllegalArgumentException.class, () -> delivery(0, 0L, 0L, 0));
        assertThrows(IllegalArgumentException.class, () -> delivery(1, 0L, 0L, -1));
    }

    private CpfRuntimeDelivery delivery(int schemaVersion, long desiredVersion, long fencingToken, int attempt) {
        return new CpfRuntimeDelivery(
                "delivery", "change", "TYPE", "instance", desiredVersion, fencingToken,
                "request-hash", "payload-hash", schemaVersion, null, attempt, Instant.MAX);
    }
}
