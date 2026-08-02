package com.cpf.starter.base;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfCapabilityBindingRegistryTest {
    @Test void resolvesSingleDefault() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(new CpfCapabilityBinding("messaging", "primary", "kafka", true, Map.of()));
        assertThat(registry.requireDefault("messaging").provider()).isEqualTo("kafka");
    }

    @Test void rejectsAmbiguousDefaults() {
        var registry = new CpfCapabilityBindingRegistry();
        registry.register(new CpfCapabilityBinding("messaging", "kafka", "kafka", true, Map.of()));
        assertThatThrownBy(() -> registry.register(new CpfCapabilityBinding("messaging", "rabbit", "rabbitmq", true, Map.of())))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Multiple default");
    }
}
