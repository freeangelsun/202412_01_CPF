package com.cpf.starter.runtime;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfCapabilityBindingRegistryTest {
    @Test void namedMultiAllowsMultipleBindingsWithoutDefault() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.configureCardinality("external-client", CpfCapabilityBindingCardinality.NAMED_MULTI_OPTIONAL_DEFAULT);
        registry.register(binding("external-client", "bank-a", false));
        registry.register(binding("external-client", "bank-b", false));
        assertDoesNotThrow(registry::validateAll);
        assertEquals(2, registry.list("external-client").size());
    }

    @Test void explicitOnlyRejectsDefaultAndRequiresName() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.configureCardinality("institution", CpfCapabilityBindingCardinality.EXPLICIT_ONLY);
        registry.register(binding("institution", "bank-a", false));
        assertThrows(IllegalStateException.class, () -> registry.requireDefault("institution"));
        assertThrows(IllegalStateException.class, () -> registry.register(binding("institution", "bank-b", true)));
    }

    @Test void legacyPolicyStillRequiresOneDefaultAtStartup() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(binding("messaging", "kafka-a", false));
        assertThrows(IllegalStateException.class, registry::validateAll);
    }

    private static CpfCapabilityBinding binding(String capability, String name, boolean defaultBinding) {
        return new CpfCapabilityBinding(capability, name, "test", defaultBinding, Map.of());
    }
}
