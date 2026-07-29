package com.cpf.batch.worker;

import com.cpf.batch.api.BatchExecutionPolicy;
import com.cpf.batch.api.BatchExecutorType;
import com.cpf.batch.api.JobPackManifest;
import com.cpf.batch.spi.BusinessJobProvider;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.worker.internal.JdbcWorkerExecutionRepository;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobOperator;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JobPackDispatcherFencingTest {
    @Test
    void businessExecutorIsNeverResolvedWhenTheClaimCannotBecomeRunning() {
        JobPackCatalog catalog = mock(JobPackCatalog.class);
        JobOperator jobOperator = mock(JobOperator.class);
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

        var dispatcher = new JobPackDispatcher(catalog, jobOperator, executions, leases,
                new ObjectMapper(), shell, files);

        assertThatThrownBy(() -> dispatcher.execute(lease))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Batch execution failed");

        verifyNoInteractions(catalog, jobOperator, shell, files);
        verify(leases, never()).complete(any(), anyString(), any());
    }

    @Test
    void leaseFinalizationFailureIsSurfacedAsRecoveryRequired() {
        JobPackCatalog catalog = mock(JobPackCatalog.class);
        JobOperator jobOperator = mock(JobOperator.class);
        JdbcWorkerExecutionRepository executions = mock(JdbcWorkerExecutionRepository.class);
        JdbcWorkerLeaseRepository leases = mock(JdbcWorkerLeaseRepository.class);
        ApprovedShellExecutor shell = mock(ApprovedShellExecutor.class);
        ApprovedFileExecutor files = mock(ApprovedFileExecutor.class);
        var lease = new JdbcWorkerLeaseRepository.Lease(
                11L, "worker-a", "lease-b", 3L, Instant.now().plusSeconds(30));
        when(executions.load(11L)).thenReturn(new JdbcWorkerExecutionRepository.Work(
                11L, "JOB-11", "{}", "tx-11", "segment-11", null, "tester"));
        when(executions.markRunning(lease)).thenReturn(false);
        when(executions.finish(
                lease,
                "FAILED",
                "Worker lease expired or was fenced before business execution")).thenReturn(true);
        doThrow(new IllegalStateException("lease store unavailable"))
                .when(leases)
                .complete(
                        lease,
                        "FAILED",
                        "Worker lease expired or was fenced before business execution");

        var dispatcher = new JobPackDispatcher(catalog, jobOperator, executions, leases,
                new ObjectMapper(), shell, files);

        Throwable thrown = catchThrowable(() -> dispatcher.execute(lease));

        assertThat(thrown)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("recovery is required");
        assertThat(thrown.getCause().getSuppressed())
                .singleElement()
                .satisfies(suppressed -> assertThat(suppressed)
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("lease store unavailable"));
    }

    @Test
    void successfulBusinessResultIsNotRewrittenWhenLeaseReleaseFails() throws Exception {
        JobPackCatalog catalog = mock(JobPackCatalog.class);
        BusinessJobProvider provider = mock(BusinessJobProvider.class);
        JobPackManifest.JobDefinition definition = new JobPackManifest.JobDefinition(
                "JOB-12",
                "approved shell",
                false,
                List.of(),
                null,
                null,
                BatchExecutorType.APPROVED_SHELL,
                "script-a",
                List.of(),
                BatchExecutionPolicy.defaults());
        JobPackManifest manifest = new JobPackManifest(
                "pack-a",
                "REF",
                "com.example:pack-a",
                "1.0.0",
                "checksum",
                null,
                "[1.0,2.0)",
                List.of(),
                List.of(definition),
                java.util.Map.of());
        when(catalog.providerFor("JOB-12")).thenReturn(provider);
        when(provider.manifest()).thenReturn(manifest);

        JobOperator jobOperator = mock(JobOperator.class);
        JdbcWorkerExecutionRepository executions = mock(JdbcWorkerExecutionRepository.class);
        JdbcWorkerLeaseRepository leases = mock(JdbcWorkerLeaseRepository.class);
        ApprovedShellExecutor shell = mock(ApprovedShellExecutor.class);
        ApprovedFileExecutor files = mock(ApprovedFileExecutor.class);
        var lease = new JdbcWorkerLeaseRepository.Lease(
                12L, "worker-a", "lease-c", 4L, Instant.now().plusSeconds(30));
        when(executions.load(12L)).thenReturn(new JdbcWorkerExecutionRepository.Work(
                12L, "JOB-12", "{}", "tx-12", "segment-12", null, "tester"));
        when(executions.markRunning(lease)).thenReturn(true);
        when(shell.execute("script-a", java.util.Map.of()))
                .thenReturn(new ApprovedShellExecutor.Result(true, 0, "completed"));
        when(executions.finish(lease, "COMPLETED", "completed")).thenReturn(true);
        doThrow(new IllegalStateException("lease store unavailable"))
                .when(leases).complete(lease, "COMPLETED", "completed");
        var dispatcher = new JobPackDispatcher(catalog, jobOperator, executions, leases,
                new ObjectMapper(), shell, files);

        assertThatThrownBy(() -> dispatcher.execute(lease))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("result was persisted")
                .hasMessageContaining("recovery is required");
        verify(executions, never()).finish(lease, "FAILED", "lease store unavailable");
    }
}
