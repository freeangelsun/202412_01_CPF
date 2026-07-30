package com.cpf.core.api.parameter;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CpfParameterSchemaTest {
    @Test
    void resolvesLayersAndBlocksUnsafeOverrideWithoutLosingSecretReference() {
        var schema = new CpfParameterSchema("gateway.route", 1, "Route", "", List.of(
                new CpfParameterSchema.ParameterDefinition(
                        "timeout", CpfParameterSchema.ValueType.INTEGER, "Timeout", "", true,
                        false, false, true, 30, List.of(), "", BigDecimal.ONE,
                        new BigDecimal("300"), null, null, "", "", 1, null),
                new CpfParameterSchema.ParameterDefinition(
                        "secret", CpfParameterSchema.ValueType.SECRET_REFERENCE, "Secret", "", true,
                        true, false, false, "vault:default", List.of(), "", null, null,
                        null, null, "SECRET", "", 2, null)));

        var validation = schema.validate(Map.of("timeout", 60, "secret", "vault:default"));
        assertEquals("***", validation.normalizedValues().get("secret"));

        var result = schema.resolve(List.of(new CpfParameterSchema.ValueLayer(
                CpfParameterSchema.ValueSource.ENVIRONMENT_POLICY, Map.of("timeout", 60))));
        assertEquals(60, result.values().get("timeout"));
        assertEquals("vault:default", result.values().get("secret"));

        assertThrows(IllegalArgumentException.class, () -> schema.resolve(List.of(
                new CpfParameterSchema.ValueLayer(
                        CpfParameterSchema.ValueSource.OPERATION_OVERRIDE,
                        Map.of("secret", "vault:other")))));
    }
}
