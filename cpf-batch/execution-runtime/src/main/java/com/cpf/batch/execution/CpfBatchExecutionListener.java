package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListener;

/** Spring Batch Metadata ID를 CPF 감사 원장에 기록하는 단일 Listener입니다. */
public final class CpfBatchExecutionListener implements JobExecutionListener, StepExecutionListener {
    private final BatchExecutionLedgerPort ledger;
    private final BatchFencingPort fencing;

    public CpfBatchExecutionListener(BatchExecutionLedgerPort ledger, BatchFencingPort fencing) {
        this.ledger = ledger;
        this.fencing = fencing;
    }

    @Override
    public void beforeJob(JobExecution execution) { bind(execution, null); }

    @Override
    public void afterJob(JobExecution execution) {
        bind(execution, null);
        if (execution.getAllFailureExceptions().stream().anyMatch(CpfBatchUnknownResultException.class::isInstance)) {
            ledger.recordUnknown(required(execution, "cpfExecutionId"), "UNKNOWN_RESULT",
                    execution.getAllFailureExceptions().toString());
        }
    }

    @Override
    public void beforeStep(StepExecution execution) { bind(execution.getJobExecution(), execution); }

    @Override
    public org.springframework.batch.core.ExitStatus afterStep(StepExecution execution) {
        bind(execution.getJobExecution(), execution);
        return execution.getExitStatus();
    }

    private void bind(JobExecution jobExecution, StepExecution stepExecution) {
        String cpfExecutionId = required(jobExecution, "cpfExecutionId");
        String jobId = required(jobExecution, "jobId");
        long fencingToken = requiredLong(jobExecution, "fencingToken");
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
        ledger.bind(new BatchExecutionLink(
                cpfExecutionId,
                jobId,
                requiredLong(jobExecution, "definitionVersion"),
                jobExecution.getJobInstance().getInstanceId(),
                jobExecution.getId(),
                stepExecution == null ? null : stepExecution.getId(),
                stepExecution == null ? jobExecution.getStatus().name() : stepExecution.getStatus().name(),
                fencingToken,
                Instant.now()));
    }

    private static String required(JobExecution execution, String name) {
        String value = execution.getJobParameters().getString(name);
        if (value == null || value.isBlank()) throw new IllegalStateException(name + " is missing");
        return value;
    }
    private static long requiredLong(JobExecution execution, String name) {
        Long value = execution.getJobParameters().getLong(name);
        if (value == null || value <= 0) throw new IllegalStateException(name + " is missing");
        return value;
    }
}
