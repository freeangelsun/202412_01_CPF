package com.cpf.local.batch;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfLocalBatchRuntimeSafetyGuardTest {

    @Test
    void rejectsWithoutExplicitLocalEnableFlag() {
        GenericApplicationContext context = new GenericApplicationContext();
        context.setEnvironment(new MockEnvironment());

        assertThatThrownBy(() -> new CpfLocalBatchRuntimeSafetyGuard().initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enabled=true");
    }

    @Test
    void rejectsNonLocalProfile() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment environment = new MockEnvironment()
                .withProperty("cpf.local.batch.enabled", "true")
                .withProperty("server.address", "127.0.0.1");
        environment.setActiveProfiles("test");
        context.setEnvironment(environment);

        assertThatThrownBy(() -> new CpfLocalBatchRuntimeSafetyGuard().initialize(context))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("local Profile");
    }

    @Test
    void acceptsLocalProfileWithLoopbackBind() {
        GenericApplicationContext context = new GenericApplicationContext();
        ConfigurableEnvironment environment = new MockEnvironment()
                .withProperty("cpf.local.batch.enabled", "true")
                .withProperty("cpf.environment", "local")
                .withProperty("server.address", "127.0.0.1");
        environment.setActiveProfiles("local");
        context.setEnvironment(environment);

        assertThatCode(() -> new CpfLocalBatchRuntimeSafetyGuard().initialize(context))
                .doesNotThrowAnyException();
    }
}
