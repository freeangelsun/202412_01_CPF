package com.cpf.batch.worker;

import com.cpf.batch.runtime.BatchRuntimePolicy;
import com.cpf.batch.api.ActualState;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkerRuntimePolicyTest {
    @Test
    void sharedRuntimePolicyChangesActualPollCapacity() {
        BatchRuntimePolicy policy = new BatchRuntimePolicy();
        try (WorkerRuntime runtime = new WorkerRuntime(null, null, "W1", "1", "GENERAL", 8, 30)) {
            runtime.setRuntimePolicy(policy);
            assertEquals(8, runtime.availableCapacity());
            org.junit.jupiter.api.Assertions.assertTrue(runtime.ready());
            policy.replaceConcurrency(1L, true, 3);
            assertEquals(3, runtime.availableCapacity());
            policy.replaceConcurrency(2L, false, 3);
            assertEquals(0, runtime.availableCapacity());
            org.junit.jupiter.api.Assertions.assertFalse(runtime.ready());
        }
    }

    @Test
    void repositoryFailureIsReportedAsDegradedUntilAProbeSucceeds() {
        JdbcWorkerLeaseRepository repository = mock(JdbcWorkerLeaseRepository.class);
        when(repository.recoverExpired())
                .thenThrow(new DataAccessResourceFailureException("database unavailable"))
                .thenReturn(new JdbcWorkerLeaseRepository.RecoveryResult(0,0));
        try (WorkerRuntime runtime = new WorkerRuntime(
                repository,
                mock(JobPackDispatcher.class),
                "W1",
                "1",
                "GENERAL",
                1,
                30)) {
            assertThatThrownBy(runtime::recoverExpired)
                    .isInstanceOf(DataAccessResourceFailureException.class);
            assertThat(runtime.actualState()).isEqualTo(ActualState.DEGRADED);
            assertThat(runtime.ready()).isFalse();
            assertThat(runtime.dependencyHealth()).containsEntry("workerRuntime","DOWN");
            assertThat(runtime.lastErrorCode()).contains("DATAACCESSRESOURCEFAILUREEXCEPTION");

            runtime.recoverExpired();

            assertThat(runtime.actualState()).isEqualTo(ActualState.READY);
            assertThat(runtime.ready()).isTrue();
        }
    }
}
