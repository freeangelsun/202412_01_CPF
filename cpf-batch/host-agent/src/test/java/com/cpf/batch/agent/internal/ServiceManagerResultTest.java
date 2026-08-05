package com.cpf.batch.agent.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ServiceManagerResultTest {
    @Test
    void compatibilityConstructorKeepsKnownResult() {
        ServiceManager.Result result = new ServiceManager.Result(false, 3, "inactive");
        assertThat(result.unknownResult()).isFalse();
    }

    @Test
    void unknownFactoryDoesNotCollapseIndeterminateOutcomeToFailure() {
        ServiceManager.Result result = ServiceManager.Result.unknown(-1, "PROCESS_TIMEOUT");
        assertThat(result.success()).isFalse();
        assertThat(result.unknownResult()).isTrue();
    }
}
