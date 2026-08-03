package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobExecutionException;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class CpfSpringBatchExecutionControlAbandonTest {
    private static final long JOB_EXECUTION_ID = 77L;
    private static final String CPF_EXECUTION_ID = "CPF-EXEC-ABANDON-77";
    private static final String OPERATOR_ID = "operator-a";
    private static final String REASON = "approved abandon after recovery review";
    private JobOperator operator;
    private JobRepository repository;
    private BatchExecutionLedgerPort ledger;
    private JobExecution execution;
    private CpfSpringBatchExecutionControl control;

    @BeforeEach
    void setUp() {
        operator = mock(JobOperator.class);
        repository = mock(JobRepository.class);
        ledger = mock(BatchExecutionLedgerPort.class);
        execution = mock(JobExecution.class);
        JobParameters parameters = new JobParametersBuilder()
                .addString("cpfExecutionId", CPF_EXECUTION_ID).toJobParameters();
        when(repository.getJobExecution(JOB_EXECUTION_ID)).thenReturn(execution);
        when(execution.getJobParameters()).thenReturn(parameters);
        control = new CpfSpringBatchExecutionControl(
                operator, repository, mock(CpfBatchJobFactory.class),
                ledger, mock(BatchFencingPort.class));
    }

    @Test
    void claimsLedgerBeforeSpringBatchAndConfirmsAfterSuccess() {
        assertDoesNotThrow(() -> control.abandon(JOB_EXECUTION_ID, OPERATOR_ID, REASON));
        InOrder order = inOrder(ledger, operator);
        order.verify(ledger).transition(
                CPF_EXECUTION_ID,
                Set.of(BatchControlState.STOPPED, BatchControlState.FAILED,
                        BatchControlState.UNKNOWN_RESULT),
                BatchControlState.ABANDONING,
                "OPERATOR_ABANDON_REQUESTED", REASON, null);
        order.verify(operator).abandon(execution);
        order.verify(ledger).transition(
                CPF_EXECUTION_ID, Set.of(BatchControlState.ABANDONING),
                BatchControlState.ABANDONED, "OPERATOR_ABANDON", REASON, null);
    }

    @Test
    void stateConflictPreventsSpringBatchSideEffect() {
        doThrow(new IllegalStateException("optimistic conflict"))
                .when(ledger).transition(
                        eq(CPF_EXECUTION_ID),
                        eq(Set.of(BatchControlState.STOPPED, BatchControlState.FAILED,
                                BatchControlState.UNKNOWN_RESULT)),
                        eq(BatchControlState.ABANDONING),
                        eq("OPERATOR_ABANDON_REQUESTED"), eq(REASON), eq(null));
        assertThrows(IllegalStateException.class,
                () -> control.abandon(JOB_EXECUTION_ID, OPERATOR_ID, REASON));
        verify(operator, never()).abandon(execution);
    }

    @Test
    void springBatchFailureBecomesUnknownAndRequiresReconcile() throws Exception {
        doAnswer(invocation -> { throw new JobExecutionException("transport interrupted"); })
                .when(operator).abandon(execution);
        assertThrows(CpfBatchUnknownResultException.class,
                () -> control.abandon(JOB_EXECUTION_ID, OPERATOR_ID, REASON));
        verify(ledger).recordUnknown(
                eq(CPF_EXECUTION_ID), eq("BATCH_ABANDON_RESPONSE_UNKNOWN"),
                eq("transport interrupted"));
        verify(ledger, never()).transition(
                eq(CPF_EXECUTION_ID), eq(Set.of(BatchControlState.ABANDONING)),
                eq(BatchControlState.ABANDONED), eq("OPERATOR_ABANDON"),
                eq(REASON), eq(null));
    }

    @Test
    void ledgerConfirmationFailureDoesNotReportSuccess() {
        doThrow(new IllegalStateException("ledger unavailable"))
                .when(ledger).transition(
                        eq(CPF_EXECUTION_ID), eq(Set.of(BatchControlState.ABANDONING)),
                        eq(BatchControlState.ABANDONED), eq("OPERATOR_ABANDON"),
                        eq(REASON), eq(null));
        assertThrows(CpfBatchUnknownResultException.class,
                () -> control.abandon(JOB_EXECUTION_ID, OPERATOR_ID, REASON));
        verify(operator).abandon(execution);
        verify(ledger).recordUnknown(
                eq(CPF_EXECUTION_ID), eq("BATCH_ABANDON_LEDGER_CONFIRM_UNKNOWN"),
                eq("ledger unavailable"));
    }
}
