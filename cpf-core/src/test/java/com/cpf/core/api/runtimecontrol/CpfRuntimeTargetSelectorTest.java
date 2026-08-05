package com.cpf.core.api.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfRuntimeTargetSelectorTest {

    @Test
    void normalizesScalarIdsAndLabelsWithoutChangingSelectorMeaning() {
        CpfRuntimeTargetSelector selector = new CpfRuntimeTargetSelector(
                " prod ", " account ", " blue ",
                List.of(" instance-1 ", "instance-1", "instance-2"),
                List.of(" instance-9 "), Map.of(" zone ", " east "),
                " az-1 ", " cell-a ", false, false, false);

        assertEquals("prod", selector.environment());
        assertEquals(List.of("instance-1", "instance-2"), selector.instanceIds());
        assertEquals(List.of("instance-9"), selector.excludeInstanceIds());
        assertEquals(Map.of("zone", "east"), selector.labels());
    }

    @Test
    void rejectsBlankListAndLabelEntries() {
        assertThrows(IllegalArgumentException.class, () -> new CpfRuntimeTargetSelector(
                null, null, null, java.util.Arrays.asList("instance-1", " "),
                List.of(), Map.of(), null, null, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> new CpfRuntimeTargetSelector(
                null, null, null, List.of(), List.of(), Map.of("key", " "),
                null, null, false, false, false));
    }
}
