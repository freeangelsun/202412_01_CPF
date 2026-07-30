package com.cpf.batch.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BatchParameterDefinitionTest {

    @Test
    void keepsLegacyConstructorCompatible() {
        BatchParameterDefinition definition = new BatchParameterDefinition("businessDate", "DATE", true, null, true, false);
        assertTrue(definition.validate("2026-07-30").valid());
        assertFalse(definition.validate("20260730").valid());
    }

    @Test
    void validatesEnumAndRangeWithoutLeakingSecret() {
        BatchParameterDefinition enumDefinition = new BatchParameterDefinition(
                "mode", "ENUM", true, "SAFE", true, false,
                "실행 모드", "", List.of("SAFE", "FAST"), "", "", "", 1, 10,
                "", "", true);
        assertTrue(enumDefinition.validate("SAFE").valid());
        assertEquals("NOT_ALLOWED", enumDefinition.validate("RAW").code());

        BatchParameterDefinition secret = new BatchParameterDefinition(
                "credential", "SECRET_REFERENCE", true, null, false, true,
                "인증정보", "", List.of(), "", "", "", null, null,
                "SECRET_REFERENCE", "", false);
        BatchParameterDefinition.ValidationResult result = secret.validate("vault:batch/prod");
        assertTrue(result.valid());
        assertEquals("***", result.normalizedValue());
    }

    @Test
    void rejectsInvalidSchemaAtConstructionTime() {
        assertThrows(IllegalArgumentException.class, () -> new BatchParameterDefinition(
                "mode", "ENUM", true, null, false, false,
                "mode", "", List.of(), "", "", "", null, null,
                "", "", true));
        assertThrows(IllegalArgumentException.class, () -> new BatchParameterDefinition(
                "mode", "UNSUPPORTED", true, null, false, false));
    }
}
