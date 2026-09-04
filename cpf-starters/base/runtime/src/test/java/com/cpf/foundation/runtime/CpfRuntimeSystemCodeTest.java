package com.cpf.foundation.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.mock.env.MockEnvironment;

class CpfRuntimeSystemCodeTest {

    @Test
    void acceptsTheLiteralCanonicalSystemCode() {
        MockEnvironment environment = canonicalRuntime("BUSINESS_DOMAIN", "MBR");

        assertThat(CpfRuntimeSystemCode.resolve(environment)).isEqualTo("MBR");
    }

    @Test
    void acceptsAnExternalOverrideOnlyWhenItMatchesTheCanonicalSystemCode() {
        MockEnvironment environment = canonicalRuntime("BUSINESS_DOMAIN", "MBR");
        environment.getPropertySources().addFirst(new MapPropertySource(
                "systemEnvironment", Map.of("cpf.system-code", "MBR")));

        assertThat(CpfRuntimeSystemCode.resolve(environment)).isEqualTo("MBR");
    }

    @Test
    void rejectsAnExternalOverrideThatChangesTheCanonicalSystemCode() {
        MockEnvironment environment = canonicalRuntime("BUSINESS_DOMAIN", "MBR");
        environment.getPropertySources().addFirst(new MapPropertySource(
                "systemEnvironment", Map.of("cpf.system-code", "EXS")));

        assertThatThrownBy(() -> CpfRuntimeSystemCode.resolve(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("override does not match");
    }

    @Test
    void rejectsAnExternalSystemCodeWhenTheRoleHasNoSystemIdentity() {
        MockEnvironment environment = canonicalRuntime("TOPOLOGY", null);
        environment.getPropertySources().addFirst(new MapPropertySource(
                "systemEnvironment", Map.of("cpf.system-code", "LOCAL")));

        assertThatThrownBy(() -> CpfRuntimeSystemCode.resolve(environment))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("external override alone");
    }

    private static MockEnvironment canonicalRuntime(String role, String systemCode) {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty(CpfRuntimeSystemCode.ARCHITECTURE_ROLE_PROPERTY, role);
        if (systemCode != null) {
            environment.setProperty("cpf.system-code", systemCode);
        }
        return environment;
    }
}
