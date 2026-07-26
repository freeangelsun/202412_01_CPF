package com.cpf.batch.worker;

import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.worker.internal.JdbcWorkerExecutionRepository;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobLauncher;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JobPackDispatcherFencingTest {
    @Test
    void businessExecutorIsNeverResolvedWhenTheClaimCannotBecomeRunning() {
        JobPackCatalog catalog = mock(JobPackCatalog.class);
        JobLauncher launcher = mock(JobLauncher.class);
        JdbcWorkerExecutionRepository executions = mock(JdbcWorkerExecutionRepository.class);
        JdbcWorkerLeaseRepository leases = mock(JdbcWorkerLeaseRepository.class);
        ApprovedShellExecutor shell = mock(ApprovedShellExecutor.class);
        ApprovedFileExecutor files = mock(ApprovedFileExecutor.class);
        var lease = new JdbcWorkerLeaseRepository.Lease(
                10L, "worker-a", "lease-a", 2L, Instant.now().plusSeconds(30));
        when(executions.load(10L)).thenReturn(new JdbcWorkerExecutionRepository.Work(
                10L, "JOB-10", "{}", "tx-10", "segment-10", null, "tester"));
        when(executions.markRunning(lease)).thenReturn(false);
        when(executions.finish(lease, "FAILED",
                "Worker lease expired or was fenced before business execution")).thenReturn(false);

        var dispatcher = new JobPackDispatcher(catalog, launcher, executions, leases,
                new ObjectMapper(), shell, files);

        assertThatThrownBy(() -> dispatcher.execute(lease))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Batch execution failed");

        verifyNoInteractions(catalog, launcher, shell, files);
        verify(leases, never()).complete(any(), anyString(), any());
    }
}
