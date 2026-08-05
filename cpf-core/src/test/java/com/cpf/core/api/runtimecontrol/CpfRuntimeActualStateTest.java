package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeActualStateTest {

    @Test
    void normalizesDurableRecoveryProofIdentity() {
        CpfRuntimeActualState state = new CpfRuntimeActualState(
                " reconciliation ", 7L, " actual-hash ", " delivery-7 ");

        assertEquals("RECONCILIATION", state.changeType());
        assertEquals("actual-hash", state.actualHash());
        assertEquals("delivery-7", state.sourceDeliveryId());
    }

    @Test
    void rejectsIncompleteOrNegativeRecoveryProof() {
        assertThrows(IllegalArgumentException.class,
                () -> new CpfRuntimeActualState(" ", 1L, "hash", "delivery"));
        assertThrows(IllegalArgumentException.class,
                () -> new CpfRuntimeActualState("TYPE", -1L, "hash", "delivery"));
        assertThrows(IllegalArgumentException.class,
                () -> new CpfRuntimeActualState("TYPE", 1L, " ", "delivery"));
        assertThrows(IllegalArgumentException.class,
                () -> new CpfRuntimeActualState("TYPE", 1L, "hash", " "));
    }
}
