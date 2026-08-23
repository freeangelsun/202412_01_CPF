package com.cpf.security.secret;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.cpf.security.api.secret.CpfSecretProvider;
import com.cpf.security.api.secret.CpfSecretReference;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfEnvironmentSecretProviderTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfSecretAutoConfiguration.class));

    @Test
    void resolvesExactEnvironmentKeyAndRedactsValue() {
        CpfEnvironmentSecretProvider provider = new CpfEnvironmentSecretProvider(
                Map.of("CPF_TEST_SECRET", "secret-value")::get);
        CpfSecretReference reference = new CpfSecretReference("env", "CPF_TEST_SECRET");

        try (var value = provider.resolve(reference)) {
            assertThat(value.copy()).containsExactly("secret-value".toCharArray());
            assertThat(value.toString()).isEqualTo("[REDACTED]");
        }
        assertThat(provider.metadata(reference).attributes())
                .containsEntry("source", "process-environment")
                .containsEntry("mutable", "false");
    }

    @Test
    void rejectsUnsafeOrMissingEnvironmentKeysWithoutExposingValues() {
        CpfEnvironmentSecretProvider provider = new CpfEnvironmentSecretProvider(key -> null);
        assertThatThrownBy(() -> provider.resolve(new CpfSecretReference("env", "path/to/secret")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exact upper-case variable name");
        assertThatThrownBy(() -> provider.resolve(new CpfSecretReference("env", "CPF_MISSING_SECRET")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Environment secret is not configured: CPF_MISSING_SECRET");
    }

    @Test
    void explicitEnvironmentProviderEnablesRegistry() {
        contextRunner.withPropertyValues("cpf.security.secret.environment.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).hasSingleBean(CpfSecretProvider.class);
                    assertThat(context.getBean(CpfSecretProvider.class).providerId()).isEqualTo("env");
                    assertThat(context).hasSingleBean(CpfSecretProviderRegistry.class);
                });
    }

    @Test
    void absenceOfApprovedProviderRemainsFailClosed() {
        contextRunner.run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseMessage(
                    "CPF Product runtime requires an approved customer-managed SecretProvider.");
        });
    }
}
