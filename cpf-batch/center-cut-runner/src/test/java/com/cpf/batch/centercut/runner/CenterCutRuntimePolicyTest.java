package com.cpf.batch.centercut.runner;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.api.ActualState;
import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CenterCutRuntimePolicyTest {
    @Test
    void sharedPolicyControlsActualCenterCutPollGate() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        try (CenterCutRuntime runtime = new CenterCutRuntime(null, null, "R1", "P1", 30)) {
            runtime.setRuntimePolicy(policy);
            assertTrue(runtime.runtimeEnabled());
            assertTrue(runtime.ready());
            org.junit.jupiter.api.Assertions.assertEquals(1,runtime.availableCapacity());
            policy.replaceCenterCut(1L, false);
            assertFalse(runtime.runtimeEnabled());
            assertTrue(runtime.draining());
            assertFalse(runtime.ready());
            org.junit.jupiter.api.Assertions.assertEquals(0,runtime.availableCapacity());
        }
    }

    @Test
    void repositoryFailureIsReportedAsDegradedUntilRecoveryProbeSucceeds() {
        JdbcCenterCutClaimRepository repository = mock(JdbcCenterCutClaimRepository.class);
        when(repository.recoverExpiredToUnknown())
                .thenThrow(new DataAccessResourceFailureException("database unavailable"))
                .thenReturn(0);
        try (CenterCutRuntime runtime = new CenterCutRuntime(
                repository,
                mock(CenterCutDispatcher.class),
                "R1",
                "P1",
                30)) {
            assertThatThrownBy(runtime::recover)
                    .isInstanceOf(DataAccessResourceFailureException.class);
            assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
            assertThat(runtime.ready()).isFalse();
            assertThat(runtime.dependencyHealth()).containsEntry("centerCutRuntime","DOWN");
            assertThat(runtime.lastErrorCode()).contains("DATAACCESSRESOURCEFAILUREEXCEPTION");

            runtime.recover();

            assertThat(runtime.actualState()).isEqualTo(ActualState.READY);
            assertThat(runtime.ready()).isTrue();
        }
    }
}
