package com.cpf.local.runtime;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfLocalRuntimeSafetyGuardTest {

    @Test
    void rejectsWhenDevelopmentEnableFlagIsMissing() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());

        assertThatThrownBy(() -> new CpfLocalRuntimeSafetyGuard().initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enabled=true");
    }

    @Test
    void rejectsProductionProfileEvenWhenEnabled() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment environment = new MockEnvironment()
                .withProperty("cpf.local.runtime.enabled", "true")
                .withProperty("server.address", "127.0.0.1");
        environment.setActiveProfiles("prod");
        context.setEnvironment(environment);

        assertThatThrownBy(() -> new CpfLocalRuntimeSafetyGuard().initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Production/Stage");
    }

    @Test
    void acceptsExplicitLocalProfileAndLoopbackBind() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment environment = new MockEnvironment()
                .withProperty("cpf.local.runtime.enabled", "true")
                .withProperty("cpf.environment", "local")
                .withProperty("server.address", "127.0.0.1");
        environment.setActiveProfiles("local", "local-standard");
        context.setEnvironment(environment);

        assertThatCode(() -> new CpfLocalRuntimeSafetyGuard().initialize(context))
                .doesNotThrowAnyException();
    }
}
