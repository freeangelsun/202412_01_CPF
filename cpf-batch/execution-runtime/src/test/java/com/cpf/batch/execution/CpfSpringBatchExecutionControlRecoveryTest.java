package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionReservation;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class CpfSpringBatchExecutionControlRecoveryTest {
    @Test
    void rejectsStaleRecoveryBeforeCallingSpringBatch() {
        Fixture fixture = new Fixture();
        doThrow(new IllegalStateException("stale fence"))
                .when(fixture.fencing).assertCurrent("BAT.RECOVER", "CPF-EXEC-1", 17L);

        assertThrows(IllegalStateException.class,
                () -> fixture.control.recover(101L, "operator-a", "approved recovery"));

        verify(fixture.operator, never()).recover(fixture.previous);
        verify(fixture.ledger, never()).recordUnknown(
                "CPF-EXEC-1", "BATCH_RECOVER_RESPONSE_UNKNOWN", "stale fence");
    }

    @Test
    void recordsUnknownWhenRecoverResponseIsLost() throws Exception {
        Fixture fixture = new Fixture();
        when(fixture.operator.recover(fixture.previous))
                .thenThrow(new JobExecutionException("response lost token=secret"));

        CpfBatchUnknownResultException failure = assertThrows(CpfBatchUnknownResultException.class,
                () -> fixture.control.recover(101L, "operator-a", "approved recovery"));

        assertEquals("BATCH_RECOVER_RESPONSE_UNKNOWN", failure.getMessage().contains("Recover outcome")
                ? "BATCH_RECOVER_RESPONSE_UNKNOWN" : "unexpected");
        verify(fixture.fencing).assertCurrent("BAT.RECOVER", "CPF-EXEC-1", 17L);
        verify(fixture.ledger).recordUnknown(
                "CPF-EXEC-1", "BATCH_RECOVER_RESPONSE_UNKNOWN", "response lost token=<masked>");
    }

    @Test
    void bindsRecoveredExecutionUsingTheValidatedFence() throws Exception {
        Fixture fixture = new Fixture();
        JobExecution recovered = execution(202L);
        when(fixture.operator.recover(fixture.previous)).thenReturn(recovered);

        BatchExecutionLink link = fixture.control.recover(
                101L, "operator-a", "approved recovery");

        assertEquals(17L, link.fencingToken());
        assertEquals(202L, link.jobExecutionId());
        verify(fixture.fencing).assertCurrent("BAT.RECOVER", "CPF-EXEC-1", 17L);
        verify(fixture.ledger).bind(link);
    }


    @Test
    void reconcileFallsBackWhenLinkedSpringBatchMetadataWasDeleted() {
        Fixture fixture = new Fixture();
        BatchExecutionReservation reservation = reservation();
        JobExecution recovered = execution(202L);
        JobInstance instance = recovered.getJobInstance();
        BatchExecutionLink stale = link(999L, "CPF-EXEC-1");
        when(fixture.ledger.findReservation("CPF-EXEC-1")).thenReturn(Optional.of(reservation));
        when(fixture.ledger.findByCpfExecutionId("CPF-EXEC-1")).thenReturn(List.of(stale));
        when(fixture.repository.getJobExecution(999L)).thenReturn(null);
        when(fixture.repository.getJobInstances(anyString(), eq(0), eq(100))).thenReturn(List.of(instance));
        when(fixture.repository.getJobExecutions(instance)).thenReturn(List.of(recovered));

        BatchExecutionLink reconciled = fixture.control.reconcile("CPF-EXEC-1");

        assertEquals(202L, reconciled.jobExecutionId());
        verify(fixture.ledger).bind(reconciled);
    }

    @Test
    void reconcileRejectsCrossExecutionLinkAndRecoversTheMatchingMetadata() {
        Fixture fixture = new Fixture();
        BatchExecutionReservation reservation = reservation();
        JobExecution mismatched = execution(999L, "CPF-OTHER");
        JobExecution recovered = execution(203L);
        JobInstance instance = recovered.getJobInstance();
        when(fixture.ledger.findReservation("CPF-EXEC-1")).thenReturn(Optional.of(reservation));
        when(fixture.ledger.findByCpfExecutionId("CPF-EXEC-1"))
                .thenReturn(List.of(link(999L, "CPF-EXEC-1")));
        when(fixture.repository.getJobExecution(999L)).thenReturn(mismatched);
        when(fixture.repository.getJobInstances(anyString(), eq(0), eq(100))).thenReturn(List.of(instance));
        when(fixture.repository.getJobExecutions(instance)).thenReturn(List.of(recovered));

        BatchExecutionLink reconciled = fixture.control.reconcile("CPF-EXEC-1");

        assertEquals(203L, reconciled.jobExecutionId());
        assertEquals("CPF-EXEC-1", reconciled.cpfExecutionId());
    }

    @Test
    void reconcileUsesAValidLinkedExecutionWithoutScanningOtherInstances() {
        Fixture fixture = new Fixture();
        BatchExecutionReservation reservation = reservation();
        JobExecution linked = execution(204L);
        when(fixture.ledger.findReservation("CPF-EXEC-1")).thenReturn(Optional.of(reservation));
        when(fixture.ledger.findByCpfExecutionId("CPF-EXEC-1"))
                .thenReturn(List.of(link(204L, "CPF-EXEC-1")));
        when(fixture.repository.getJobExecution(204L)).thenReturn(linked);

        BatchExecutionLink reconciled = fixture.control.reconcile("CPF-EXEC-1");

        assertEquals(204L, reconciled.jobExecutionId());
        verify(fixture.repository, never()).getJobInstances(anyString(), eq(0), eq(100));
    }

    private static final class Fixture {
        private final JobOperator operator = mock(JobOperator.class);
        private final JobRepository repository = mock(JobRepository.class);
        private final CpfBatchJobFactory jobs = mock(CpfBatchJobFactory.class);
        private final BatchExecutionLedgerPort ledger = mock(BatchExecutionLedgerPort.class);
        private final BatchFencingPort fencing = mock(BatchFencingPort.class);
        private final JobExecution previous = execution(101L);
        private final CpfSpringBatchExecutionControl control;

        private Fixture() {
            when(repository.getJobExecution(101L)).thenReturn(previous);
            control = new CpfSpringBatchExecutionControl(operator, repository, jobs, ledger, fencing);
        }
    }

    private static BatchExecutionReservation reservation() {
        return new BatchExecutionReservation(
                "CPF-EXEC-1",
                "BAT.RECOVER",
                4L,
                "APR-1",
                "JOB",
                "IDEM-1",
                "b".repeat(64),
                "a".repeat(64),
                17L,
                BatchControlState.UNKNOWN_RESULT,
                null,
                null,
                0,
                null,
                Instant.parse("2026-08-05T00:00:00Z"));
    }

    private static BatchExecutionLink link(long jobExecutionId, String cpfExecutionId) {
        return new BatchExecutionLink(
                cpfExecutionId,
                "BAT.RECOVER",
                4L,
                100L,
                jobExecutionId,
                null,
                "STARTED",
                17L,
                Instant.parse("2026-08-05T00:00:00Z"));
    }

    private static JobExecution execution(long id) {
        return execution(id, "CPF-EXEC-1");
    }

    private static JobExecution execution(long id, String cpfExecutionId) {
        JobExecution execution = mock(JobExecution.class);
        JobInstance instance = mock(JobInstance.class);
        JobParameters parameters = new JobParametersBuilder()
                .addString("cpfExecutionId", cpfExecutionId)
                .addString("jobId", "BAT.RECOVER")
                .addLong("definitionVersion", 4L)
                .addLong("fencingToken", 17L)
                .toJobParameters();
        when(execution.getId()).thenReturn(id);
        when(execution.getJobParameters()).thenReturn(parameters);
        when(execution.getJobInstance()).thenReturn(instance);
        when(execution.getStatus()).thenReturn(BatchStatus.STARTED);
        when(instance.getInstanceId()).thenReturn(id - 1L);
        return execution;
    }
}
