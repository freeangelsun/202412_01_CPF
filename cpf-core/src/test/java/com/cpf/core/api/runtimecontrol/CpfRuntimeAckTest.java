package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeAckTest {

    @Test
    void carriesDeliveryAttemptForStaleAckFencing() {
        CpfRuntimeAck ack = new CpfRuntimeAck(
                "delivery-1", "change-1", "instance-1", 7L, 3,
                11L, "actual-hash", "SUCCESS", null, "ok", Instant.EPOCH);

        assertEquals(3, ack.attempt());
        assertEquals(CpfRuntimeAckState.SUCCESS.name(), ack.state());
    }

    @Test
    void legacyConstructorUsesFirstClaimSentinel() {
        CpfRuntimeAck ack = new CpfRuntimeAck(
                "delivery-1", "change-1", "instance-1", 7L,
                11L, "actual-hash", "SUCCESS", null, "ok", Instant.EPOCH);

        assertEquals(0, ack.attempt());
    }

    @Test
    void rejectsNegativeAttempt() {
        assertThrows(IllegalArgumentException.class, () -> new CpfRuntimeAck(
                "delivery-1", "change-1", "instance-1", 7L, -1,
                11L, "actual-hash", "SUCCESS", null, "ok", Instant.EPOCH));
    }
}
