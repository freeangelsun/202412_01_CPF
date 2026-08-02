package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchCanonicalDigest;
import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionPlan;
import com.cpf.batch.api.BatchExecutionReservation;
import com.cpf.batch.api.BatchExecutionTopology;
import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class CpfSpringBatchExecutionControlParametersTest {
    @Test
    void publishesTrustedExecutorSnapshotAndNamespacesUserArguments() {
        BatchApprovedLaunchRequest request = request();

        JobParameters parameters = CpfSpringBatchExecutionControl.parameters(request, "BAT-EXECUTION-1");

        assertAll(
                () -> assertEquals("BAT.TRUSTED", parameters.getString("jobId")),
                () -> assertEquals(7L, parameters.getLong("definitionVersion")),
                () -> assertEquals("d".repeat(64), parameters.getString("definitionChecksum")),
                () -> assertEquals("SERVICE_CALL", parameters.getString("executorType")),
                () -> assertEquals("SERVICE:trusted-operation", parameters.getString("executorReference")),
                () -> assertEquals(90L, parameters.getLong("timeoutSeconds")),
                () -> assertEquals(4L, parameters.getLong("maxAttempts")),
                () -> assertEquals("T10:2026-08-02", parameters.getString("arg.businessDate")),
                () -> assertTrue(parameters.getParameter("jobId").identifying()),
                () -> assertFalse(parameters.getParameter("executorType").identifying()),
                () -> assertFalse(parameters.getParameter("maxAttempts").identifying()));
    }

    @Test
    void reconcilesThroughTheSpringBatchSixJobRepositoryReadContract() {
        JobOperator operator = mock(JobOperator.class);
        JobRepository repository = mock(JobRepository.class);
        CpfBatchJobFactory jobs = mock(CpfBatchJobFactory.class);
        BatchExecutionLedgerPort ledger = mock(BatchExecutionLedgerPort.class);
        BatchFencingPort fencing = mock(BatchFencingPort.class);
        String cpfExecutionId = "CPF-EXECUTION-1";
        String checksum = "a".repeat(64);
        Instant now = Instant.parse("2026-08-02T00:00:00Z");
        BatchExecutionReservation reservation = new BatchExecutionReservation(
                cpfExecutionId, "BAT.JOB", 3L, "APR-1", "BAT.JOB", "IDEM-1",
                "b".repeat(64), checksum, 11L, BatchControlState.UNKNOWN_RESULT,
                null, null, 0, null, now);
        JobInstance instance = mock(JobInstance.class);
        JobExecution execution = mock(JobExecution.class);
        JobParameters parameters = new JobParametersBuilder()
                .addString("cpfExecutionId", cpfExecutionId)
                .toJobParameters();
        when(ledger.findReservation(cpfExecutionId)).thenReturn(Optional.of(reservation));
        when(ledger.findByCpfExecutionId(cpfExecutionId)).thenReturn(List.of());
        String jobName = CpfBatchJobFactory.jobName("BAT.JOB", 3L, checksum);
        when(repository.getJobInstances(jobName, 0, 100)).thenReturn(List.of(instance));
        when(repository.getJobExecutions(instance)).thenReturn(List.of(execution));
        when(execution.getJobParameters()).thenReturn(parameters);
        when(execution.getId()).thenReturn(501L);
        when(execution.getJobInstance()).thenReturn(instance);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(instance.getInstanceId()).thenReturn(401L);
        CpfSpringBatchExecutionControl control = new CpfSpringBatchExecutionControl(
                operator, repository, jobs, ledger, fencing);

        BatchExecutionLink link = control.reconcile(cpfExecutionId);

        assertEquals(501L, link.jobExecutionId());
        assertEquals(401L, link.jobInstanceId());
        assertEquals("COMPLETED", link.status());
        verify(repository).getJobInstances(jobName, 0, 100);
        verify(repository).getJobExecutions(instance);
        verify(ledger).bind(link);
    }

    private static BatchApprovedLaunchRequest request() {
        BatchStepDefinition step = new BatchStepDefinition(
                "invoke", BatchJobDefinition.ExecutorType.SERVICE_CALL,
                "SERVICE:trusted-operation", Map.of(), 1, "", "", true);
        long version = 7L;
        String planId = "BAT.TRUSTED";
        String planChecksum = BatchCanonicalDigest.planHash(
                planId, version, BatchExecutionTopology.LOCAL, List.of(step));
        BatchExecutionPlan plan = new BatchExecutionPlan(
                planId, version, BatchExecutionTopology.LOCAL, List.of(step), planChecksum);
        BatchJobDefinition definition = new BatchJobDefinition(
                planId, version, "Trusted snapshot", BatchJobDefinition.ExecutorType.SERVICE_CALL,
                BatchJobDefinition.State.PUBLISHED, "BAT", "trusted snapshot contract",
                new BatchJobDefinition.Trigger(
                        BatchJobDefinition.TriggerType.MANUAL, "", "Asia/Seoul",
                        BatchJobDefinition.MisfirePolicy.FAIL_CLOSED, true),
                List.of(), List.of(),
                new BatchJobDefinition.ResourcePolicy("DEFAULT", "", 1, 90L, 0L, 0),
                new BatchJobDefinition.RecoveryPolicy(
                        4, 1L, 2.0, 10L, 0, true,
                        BatchJobDefinition.UnknownResultPolicy.RECONCILE, ""),
                BatchJobDefinition.AlertPolicy.defaults(),
                "SERVICE:trusted-operation", "d".repeat(64), "maker", "approved test definition",
                OffsetDateTime.parse("2026-08-02T00:00:00+09:00"), null, 1L);
        return new BatchApprovedLaunchRequest(
                definition, plan, Map.of("businessDate", LocalDate.of(2026, 8, 2)),
                "APR-20260802-0001", "operator-a", "approved test execution",
                "idem-20260802-0001", 11L);
    }
}
