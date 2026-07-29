package com.cpf.batch.runtime;

import com.cpf.batch.api.ActualState;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RuntimeStateProviderFailClosedTest {
    @Test
    void missingRoleProviderNeverReportsAnonymousReadyState() {
        RuntimeStateProvider provider = new RuntimeStateProvider() {
        };

        assertThat(provider.actualState()).isEqualTo(ActualState.UNKNOWN);
        assertThat(provider.ready()).isFalse();
        assertThat(provider.availableCapacity()).isZero();
        assertThat(provider.dependencyHealth()).containsEntry("runtimeStateProvider", "NOT_CONFIGURED");
        assertThat(provider.lastErrorCode()).isEqualTo("BAT_RUNTIME_STATE_PROVIDER_NOT_CONFIGURED");
    }
}
