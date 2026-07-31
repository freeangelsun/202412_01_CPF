package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;

/** Spring Batch Metadata ID를 CPF 감사 원장에 기록하는 Listener입니다. */
public final class CpfBatchExecutionListener implements JobExecutionListener, StepExecutionListener {
    private static final Logger log = LoggerFactory.getLogger(CpfBatchExecutionListener.class);
    private final BatchExecutionLedgerPort ledger;
    private final BatchFencingPort fencing;

    public CpfBatchExecutionListener(BatchExecutionLedgerPort ledger, BatchFencingPort fencing) {
        this.ledger = ledger;
        this.fencing = fencing;
    }

    /** 실행 시작 전 Fence 확인 실패는 side effect를 막기 위해 fail-closed합니다. */
    @Override
    public void beforeJob(JobExecution execution) {
        assertFence(execution);
        try {
            ledger.bind(jobLink(execution));
        } catch (RuntimeException observationFailure) {
            recordObservationFailure(execution, "BATCH_JOB_START_LEDGER_OBSERVATION_FAILED", observationFailure);
        }
    }

    /** 완료 후 관측 원장 실패는 완료된 업무 결과를 되돌리지 않고 UNKNOWN_RESULT 대사 대상으로 분리합니다. */
    @Override
    public void afterJob(JobExecution execution) {
        try {
            ledger.bind(jobLink(execution));
            if (execution.getAllFailureExceptions().stream().anyMatch(CpfBatchUnknownResultException.class::isInstance)) {
                ledger.recordUnknown(required(execution, "cpfExecutionId"), "UNKNOWN_RESULT",
                        safe(execution.getAllFailureExceptions().toString()));
            }
        } catch (RuntimeException observationFailure) {
            recordObservationFailure(execution, "BATCH_JOB_LEDGER_OBSERVATION_FAILED", observationFailure);
        }
    }

    @Override
    public void beforeStep(StepExecution execution) {
        assertFence(execution.getJobExecution());
        try {
            ledger.bind(stepLink(execution));
        } catch (RuntimeException observationFailure) {
            recordObservationFailure(execution.getJobExecution(),
                    "BATCH_STEP_START_LEDGER_OBSERVATION_FAILED", observationFailure);
        }
    }

    @Override
    public ExitStatus afterStep(StepExecution execution) {
        try {
            ledger.bind(stepLink(execution));
        } catch (RuntimeException observationFailure) {
            recordObservationFailure(execution.getJobExecution(),
                    "BATCH_STEP_LEDGER_OBSERVATION_FAILED", observationFailure);
        }
        return execution.getExitStatus();
    }

    private void assertFence(JobExecution execution) {
        fencing.assertCurrent(required(execution, "jobId"), required(execution, "cpfExecutionId"),
                requiredLong(execution, "fencingToken"));
    }

    private BatchExecutionLink jobLink(JobExecution execution) {
        return new BatchExecutionLink(
                required(execution, "cpfExecutionId"),
                required(execution, "jobId"),
                requiredLong(execution, "definitionVersion"),
                execution.getJobInstance() == null ? null : execution.getJobInstance().getInstanceId(),
                execution.getId(),
                null,
                execution.getStatus().name(),
                requiredLong(execution, "fencingToken"),
                Instant.now());
    }

    private BatchExecutionLink stepLink(StepExecution execution) {
        JobExecution job = execution.getJobExecution();
        return new BatchExecutionLink(
                required(job, "cpfExecutionId"),
                required(job, "jobId"),
                requiredLong(job, "definitionVersion"),
                job.getJobInstance() == null ? null : job.getJobInstance().getInstanceId(),
                job.getId(),
                execution.getId(),
                execution.getStatus().name(),
                requiredLong(job, "fencingToken"),
                Instant.now());
    }

    private void recordObservationFailure(JobExecution execution, String code, RuntimeException failure) {
        String cpfExecutionId;
        try {
            cpfExecutionId = required(execution, "cpfExecutionId");
        } catch (RuntimeException missingIdentity) {
            log.error("{} without cpfExecutionId", code, failure);
            return;
        }
        log.error("{} cpfExecutionId={}", code, cpfExecutionId, failure);
        try {
            ledger.recordUnknown(cpfExecutionId, code, safe(failure.getMessage()));
        } catch (RuntimeException secondary) {
            log.error("BATCH_UNKNOWN_RESULT_RECORD_FAILED cpfExecutionId={}", cpfExecutionId, secondary);
        }
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

    private static String safe(String value) {
        if (value == null) return "";
        String result = value.replaceAll(
                        "(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+",
                        "$1=<masked>")
                .replaceAll("[\\r\\n\\t]+", " ")
                .trim();
        return result.length() <= 2_000 ? result : result.substring(0, 2_000);
    }
}
