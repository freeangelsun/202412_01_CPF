package com.cpf.core.api.runtimecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CpfRuntimeChangeCommandTest {
    @Test
    void nullRolloutValuesUseDefaultsAndExplicitInvalidValuesFail() {
        CpfRuntimeChangeCommand defaults = command(1, null, null);
        assertEquals(1, defaults.waveSize());
        assertEquals(100, defaults.quorumPercent());

        assertThrows(IllegalArgumentException.class, () -> command(1, 0, 100));
        assertThrows(IllegalArgumentException.class, () -> command(1, 1, 101));
        assertThrows(IllegalArgumentException.class, () -> command(0, 1, 100));
    }

    private CpfRuntimeChangeCommand command(int schemaVersion, Integer waveSize, Integer quorumPercent) {
        return new CpfRuntimeChangeCommand(
                "operation", "TEST", schemaVersion,
                new CpfRuntimeTargetSelector(null, null, null, java.util.List.of("instance"),
                        java.util.List.of(), java.util.Map.of(), null, null, false, false, false),
                CpfRuntimePayload.empty(), 0L, "WAVE", waveSize, quorumPercent,
                null, java.time.Instant.now().plusSeconds(60), "reason", null, null, "operator");
    }
}
