package com.cpf.core.service.feature;

import com.cpf.core.api.feature.CpfFeatureFlagContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfPropertyFeatureFlagProviderTest {
    @Test
    void undefinedFlagReturnsSafeDefault() {
        var provider = new CpfPropertyFeatureFlagProvider(new MockEnvironment(), new ObjectMapper());
        var result = provider.evaluate("new-ui", Boolean.class,
                new CpfFeatureFlagContext("test", "MBR", null, null, "member-1", Map.of()), false);
        assertThat(result.value()).isFalse();
        assertThat(result.defaulted()).isTrue();
    }

    @Test
    void sameTargetHasStablePercentageDecision() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.feature-flags.new-ui.enabled", "true")
                .withProperty("cpf.feature-flags.new-ui.percentage", "50")
                .withProperty("cpf.feature-flags.new-ui.value", "true");
        var provider = new CpfPropertyFeatureFlagProvider(env, new ObjectMapper());
        var context = new CpfFeatureFlagContext("test", "MBR", "T1", "APP", "member-1", Map.of());
        assertThat(provider.evaluate("new-ui", Boolean.class, context, false).value())
                .isEqualTo(provider.evaluate("new-ui", Boolean.class, context, false).value());
    }
    @Test
    void targetingAllowAndDenyListsFailClosed() {
        MockEnvironment env = new MockEnvironment()
                .withProperty("cpf.feature-flags.new-ui.enabled", "true")
                .withProperty("cpf.feature-flags.new-ui.domains", "MBR,ACC")
                .withProperty("cpf.feature-flags.new-ui.deny-channels", "JUT")
                .withProperty("cpf.feature-flags.new-ui.value", "true");
        var provider = new CpfPropertyFeatureFlagProvider(env, new ObjectMapper());

        var allowed = new CpfFeatureFlagContext("test", "MBR", "T1", "APP", "member-1", Map.of());
        var wrongDomain = new CpfFeatureFlagContext("test", "REF", "T1", "APP", "member-1", Map.of());
        var deniedChannel = new CpfFeatureFlagContext("test", "MBR", "T1", "JUT", "member-1", Map.of());

        assertThat(provider.evaluate("new-ui", Boolean.class, allowed, false).value()).isTrue();
        assertThat(provider.evaluate("new-ui", Boolean.class, wrongDomain, false).reason()).isEqualTo("TARGET_EXCLUDED");
        assertThat(provider.evaluate("new-ui", Boolean.class, deniedChannel, false).reason()).isEqualTo("TARGET_DENIED");
    }

    @Test
    void facadeReturnsSafeDefaultWhenProviderFails() {
        com.cpf.core.spi.feature.CpfFeatureFlagProvider failing = new com.cpf.core.spi.feature.CpfFeatureFlagProvider() {
            @Override
            public <T> com.cpf.core.api.feature.CpfFeatureFlagResult<T> evaluate(
                    String flagKey, Class<T> valueType, CpfFeatureFlagContext context, T safeDefault) {
                throw new IllegalStateException("provider down");
            }
        };
        var flags = new com.cpf.core.api.feature.CpfFeatureFlags(failing);
        var result = flags.bool("new-ui", new CpfFeatureFlagContext("test", "MBR", null, null, "member-1", Map.of()), false);
        assertThat(result.value()).isFalse();
        assertThat(result.defaulted()).isTrue();
        assertThat(result.reason()).isEqualTo("PROVIDER_FAILURE");
    }

}
