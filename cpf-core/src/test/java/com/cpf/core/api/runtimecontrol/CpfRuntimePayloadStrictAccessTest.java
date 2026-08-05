package com.cpf.core.api.runtimecontrol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CpfRuntimePayloadStrictAccessTest {

    @Test
    void exposesCanonicalFieldNamesAndStrictTypedValues() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse(
                "{\"z\":1,\"enabled\":false,\"flagKey\":\"payments.write.enabled\"}");

        assertEquals(List.of("enabled", "flagKey", "z"), List.copyOf(payload.fieldNames()));
        assertEquals("payments.write.enabled", payload.textStrict("flagKey", null));
        assertFalse(payload.booleanStrict("enabled", true));
        assertTrue(payload.booleanStrict("missing", true));
    }

    @Test
    void strictAccessRejectsCoercedTypes() {
        CpfRuntimePayload payload = CpfRuntimePayload.parse(
                "{\"flagKey\":1,\"enabled\":\"true\"}");

        assertThrows(IllegalArgumentException.class,
                () -> payload.textStrict("flagKey", null));
        assertThrows(IllegalArgumentException.class,
                () -> payload.booleanStrict("enabled", false));
    }
}
