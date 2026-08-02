package com.cpf.starter.base;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class CpfCapabilityBindingRegistryTest {
    @Test
    void activeCapabilityRequiresExactlyOneDefaultAtStartup() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(new CpfCapabilityBinding(
                "messaging", "kafka", "kafka", false, Map.of()));
        assertThrows(IllegalStateException.class, registry::validateAll);
    }

    @Test
    void exactlyOneDefaultPasses() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(new CpfCapabilityBinding(
                "messaging", "kafka", "kafka", true, Map.of()));
        registry.register(new CpfCapabilityBinding(
                "messaging", "rabbit", "rabbitmq", false, Map.of()));
        assertDoesNotThrow(registry::validateAll);
    }

    @Test
    void duplicateDefaultsFailDuringRegistration() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(new CpfCapabilityBinding(
                "messaging", "kafka", "kafka", true, Map.of()));
        assertThrows(IllegalStateException.class, () -> registry.register(
                new CpfCapabilityBinding(
                        "messaging", "rabbit", "rabbitmq", true, Map.of())));
    }
}
