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
        assertThat(ServiceManager.classifyStatus(result)).isEqualTo(ServiceManager.ServiceState.UNKNOWN);
    }

    @Test
    void statusClassificationPreservesStoppedAndRunningStates() {
        assertThat(ServiceManager.classifyStatus(new ServiceManager.Result(true, 0, "active")))
                .isEqualTo(ServiceManager.ServiceState.RUNNING);
        assertThat(ServiceManager.classifyStatus(new ServiceManager.Result(false, 3, "inactive")))
                .isEqualTo(ServiceManager.ServiceState.STOPPED);
        assertThat(ServiceManager.classifyStatus(new ServiceManager.Result(false, 1, "unrecognized")))
                .isEqualTo(ServiceManager.ServiceState.UNKNOWN);
    }
}
