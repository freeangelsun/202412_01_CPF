package com.cpf.core.service.reliability;

import com.cpf.core.api.feature.CpfFeatureFlagContext;
import com.cpf.core.api.feature.CpfFeatureFlagResult;
import com.cpf.core.api.feature.CpfFeatureFlags;
import com.cpf.core.spi.feature.CpfFeatureFlagProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfControlledFaultInjectorTest {
    @Test
    void targetAllowlistAndFlagMustBothEnableFault() {
        CpfFeatureFlagProvider enabledProvider = new CpfFeatureFlagProvider() {
            @Override
            public <T> CpfFeatureFlagResult<T> evaluate(
                    String flagKey, Class<T> valueType, CpfFeatureFlagContext context, T safeDefault) {
                return new CpfFeatureFlagResult<>(valueType.cast(Boolean.TRUE), "TEST_ENABLED", "test", false);
            }
        };
        CpfControlledFaultInjector injector = new CpfControlledFaultInjector(
                new CpfFeatureFlags(enabledProvider), "OREFQA0001", 0L, true);

        injector.before("OTHER");
        assertThatThrownBy(() -> injector.before("OREFQA0001"))
                .isInstanceOf(CpfInjectedFaultException.class);
    }
}
