package com.cpf.batch.execution;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.batch.execution.internal.context.CpfBatchRuntimeContexts;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

class CpfSpringBatchExecutionControlParametersTest {
    @Test
    void bindsTheApprovedBatchOwnerContextForTheWholeSynchronousLaunch() throws Exception {
        BatchApprovedLaunchRequest request = request();
        String cpfExecutionId = "BAT-EXECUTION-CONTEXT-1";
        JobOperator operator = mock(JobOperator.class);
        JobRepository repository = mock(JobRepository.class);
        CpfBatchJobFactory jobs = mock(CpfBatchJobFactory.class);
        BatchExecutionLedgerPort ledger = mock(BatchExecutionLedgerPort.class);
        BatchFencingPort fencing = mock(BatchFencingPort.class);
        CpfBatchExecutionContextSupport contextSupport = mock(CpfBatchExecutionContextSupport.class);
        Job job = mock(Job.class);
        JobInstance instance = mock(JobInstance.class);
        JobExecution execution = mock(JobExecution.class);
        CpfBatchContextBundle bundle = launchBundle(cpfExecutionId, request.fencingToken());
        BatchExecutionReservation reservation = new BatchExecutionReservation(
                cpfExecutionId, request.definition().jobId(), request.definition().definitionVersion(),
                request.approvalId(), request.idempotencyScope(), request.idempotencyKey(),
                request.requestHash(), request.plan().checksum(), request.fencingToken(),
                BatchControlState.RESERVED, null, null, 0, null, Instant.now());
        when(ledger.reserve(request)).thenReturn(cpfExecutionId);
        when(ledger.findReservation(cpfExecutionId)).thenReturn(Optional.of(reservation));
        when(jobs.materialize(request.plan())).thenReturn(job);
        when(contextSupport.launchBundle(eq(request), eq(cpfExecutionId), isNull())).thenReturn(bundle);
        when(operator.start(eq(job), any(JobParameters.class))).thenAnswer(invocation -> {
            assertSame(bundle, CpfBatchRuntimeContexts.current());
            return execution;
        });
        when(execution.getJobInstance()).thenReturn(instance);
        when(execution.getId()).thenReturn(502L);
        when(execution.getStatus()).thenReturn(BatchStatus.STARTED);
        when(instance.getInstanceId()).thenReturn(402L);
        CpfSpringBatchExecutionControl control = new CpfSpringBatchExecutionControl(
                operator, repository, jobs, ledger, fencing, contextSupport);

        assertNull(CpfBatchRuntimeContexts.current());
        BatchExecutionLink link = control.start(request);

        assertEquals(502L, link.jobExecutionId());
        assertNull(CpfBatchRuntimeContexts.current());
        verify(operator).start(eq(job), any(JobParameters.class));
        verify(ledger).bind(link);
    }

    @Test
    void publishesTrustedExecutorSnapshotAndNamespacesUserArguments() {
        BatchApprovedLaunchRequest request = request();

        JobParameters parameters = CpfSpringBatchExecutionControl.parameters(
                request, "BAT-EXECUTION-1", mock(CpfBatchExecutionContextSupport.class));

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
                operator, repository, jobs, ledger, fencing,
                mock(CpfBatchExecutionContextSupport.class));

        BatchExecutionLink link = control.reconcile(cpfExecutionId);

        assertEquals(501L, link.jobExecutionId());
        assertEquals(401L, link.jobInstanceId());
        assertEquals("COMPLETED", link.status());
        verify(repository).getJobInstances(jobName, 0, 100);
        verify(repository).getJobExecutions(instance);
        verify(ledger).bind(link);
    }


    @Test
    void reconcilesBeyondTheFirstHundredJobInstances() {
        JobOperator operator = mock(JobOperator.class);
        JobRepository repository = mock(JobRepository.class);
        CpfBatchJobFactory jobs = mock(CpfBatchJobFactory.class);
        BatchExecutionLedgerPort ledger = mock(BatchExecutionLedgerPort.class);
        BatchFencingPort fencing = mock(BatchFencingPort.class);
        String cpfExecutionId = "CPF-EXECUTION-PAGED";
        String checksum = "c".repeat(64);
        Instant now = Instant.parse("2026-08-03T00:00:00Z");
        BatchExecutionReservation reservation = new BatchExecutionReservation(
                cpfExecutionId, "BAT.PAGED", 9L, "APR-PAGED", "BAT.PAGED", "IDEM-PAGED",
                "e".repeat(64), checksum, 13L, BatchControlState.UNKNOWN_RESULT,
                null, null, 0, null, now);
        JobInstance unrelatedInstance = mock(JobInstance.class);
        JobExecution unrelatedExecution = mock(JobExecution.class);
        JobInstance targetInstance = mock(JobInstance.class);
        JobExecution targetExecution = mock(JobExecution.class);
        when(ledger.findReservation(cpfExecutionId)).thenReturn(Optional.of(reservation));
        when(ledger.findByCpfExecutionId(cpfExecutionId)).thenReturn(List.of());
        String jobName = CpfBatchJobFactory.jobName("BAT.PAGED", 9L, checksum);
        when(repository.getJobInstances(jobName, 0, 100))
                .thenReturn(Collections.nCopies(100, unrelatedInstance));
        when(repository.getJobExecutions(unrelatedInstance)).thenReturn(List.of(unrelatedExecution));
        when(unrelatedExecution.getJobParameters()).thenReturn(new JobParametersBuilder()
                .addString("cpfExecutionId", "CPF-EXECUTION-OTHER")
                .toJobParameters());
        when(repository.getJobInstances(jobName, 100, 100)).thenReturn(List.of(targetInstance));
        when(repository.getJobExecutions(targetInstance)).thenReturn(List.of(targetExecution));
        when(targetExecution.getJobParameters()).thenReturn(new JobParametersBuilder()
                .addString("cpfExecutionId", cpfExecutionId)
                .toJobParameters());
        when(targetExecution.getId()).thenReturn(901L);
        when(targetExecution.getJobInstance()).thenReturn(targetInstance);
        when(targetExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(targetInstance.getInstanceId()).thenReturn(801L);
        CpfSpringBatchExecutionControl control = new CpfSpringBatchExecutionControl(
                operator, repository, jobs, ledger, fencing,
                mock(CpfBatchExecutionContextSupport.class));

        BatchExecutionLink link = control.reconcile(cpfExecutionId);

        assertEquals(901L, link.jobExecutionId());
        assertEquals(801L, link.jobInstanceId());
        verify(repository).getJobInstances(jobName, 0, 100);
        verify(repository).getJobInstances(jobName, 100, 100);
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

    private static CpfBatchContextBundle launchBundle(String cpfExecutionId, long fencingToken) {
        Instant now = Instant.parse("2026-08-24T00:00:00Z");
        LocalDate businessDate = LocalDate.of(2026, 8, 24);
        CpfContext core = new CpfContext(
                new CpfContext.CpfTransactionContext(
                        "20260824000000000BATTEST000000001", "20260824000000000BATTEST000000001",
                        null, null, businessDate, now,
                        CpfContext.CpfTransactionOriginKind.BATCH, "cpf-batch", null),
                new CpfContext.CpfExecutionContext(
                        "batch.launch.BAT.TRUSTED", "EX-CONTEXT-1", "EX-CONTEXT-1", null,
                        "SG-CONTEXT-1", null, CpfContext.CpfExecutionType.BATCH,
                        1, 0, now, null, CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null, null, null);
        CpfBatchContext batch = new CpfBatchContext(
                "BAT.TRUSTED", "Trusted snapshot", 7, null, null, null,
                null, null, null, null, CpfBatchLaunchMode.MANUAL, businessDate,
                0, 1, null, null, null, null, null, null, null, null,
                cpfExecutionId, null, null, fencingToken, now);
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(core), batch);
    }
}
