package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
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

    private static JobExecution execution(long id) {
        JobExecution execution = mock(JobExecution.class);
        JobInstance instance = mock(JobInstance.class);
        JobParameters parameters = new JobParametersBuilder()
                .addString("cpfExecutionId", "CPF-EXEC-1")
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
