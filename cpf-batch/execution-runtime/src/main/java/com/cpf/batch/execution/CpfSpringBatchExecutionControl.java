package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameter;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

/** CPF Batch Control Plane을 Spring Batch 6 JobOperator 생명주기에 직접 연결합니다. */
public final class CpfSpringBatchExecutionControl implements BatchExecutionControlPort {
    private final JobOperator operator;
    private final JobRepository repository;
    private final CpfBatchJobFactory jobs;
    private final BatchExecutionLedgerPort ledger;
    private final BatchFencingPort fencing;

    public CpfSpringBatchExecutionControl(
            JobOperator operator,
            JobRepository repository,
            CpfBatchJobFactory jobs,
            BatchExecutionLedgerPort ledger,
            BatchFencingPort fencing) {
        this.operator = operator;
        this.repository = repository;
        this.jobs = jobs;
        this.ledger = ledger;
        this.fencing = fencing;
    }

    @Override
    public BatchExecutionLink start(BatchApprovedLaunchRequest request) {
        String cpfExecutionId = ledger.reserve(request);
        fencing.assertCurrent(request.definition().jobId(), cpfExecutionId, request.fencingToken());
        Job job = jobs.materialize(request.plan());
        JobExecution execution = operator.start(job, parameters(request, cpfExecutionId));
        BatchExecutionLink link = link(cpfExecutionId, request.definition().jobId(),
                request.definition().definitionVersion(), request.fencingToken(), execution);
        ledger.bind(link);
        return link;
    }

    @Override
    public boolean stop(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        return operator.stop(required(jobExecutionId));
    }

    @Override
    public BatchExecutionLink restart(long jobExecutionId, String operatorId, String reason, long fencingToken) {
        requireOperator(operatorId, reason);
        JobExecution previous = required(jobExecutionId);
        String cpfExecutionId = previous.getJobParameters().getString("cpfExecutionId");
        String jobId = previous.getJobParameters().getString("jobId");
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
        JobExecution restarted = operator.restart(previous);
        BatchExecutionLink link = link(cpfExecutionId, jobId,
                Objects.requireNonNull(previous.getJobParameters().getLong("definitionVersion")),
                fencingToken, restarted);
        ledger.bind(link);
        return link;
    }

    @Override
    public void abandon(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        operator.abandon(required(jobExecutionId));
    }

    @Override
    public BatchExecutionLink reconcile(String cpfExecutionId) {
        return ledger.findByCpfExecutionId(cpfExecutionId).stream()
                .max(Comparator.comparing(BatchExecutionLink::observedAt))
                .map(link -> {
                    if (link.jobExecutionId() == null) return link;
                    JobExecution execution = required(link.jobExecutionId());
                    BatchExecutionLink observed = link(
                            link.cpfExecutionId(), link.jobId(), link.definitionVersion(),
                            link.fencingToken(), execution);
                    ledger.bind(observed);
                    return observed;
                })
                .orElseThrow(() -> new IllegalArgumentException("CPF_EXECUTION_NOT_FOUND:" + cpfExecutionId));
    }

    public BatchExecutionLink recover(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        JobExecution recovered = operator.recover(required(jobExecutionId));
        String cpfExecutionId = recovered.getJobParameters().getString("cpfExecutionId");
        BatchExecutionLink link = link(cpfExecutionId,
                recovered.getJobParameters().getString("jobId"),
                Objects.requireNonNull(recovered.getJobParameters().getLong("definitionVersion")),
                Objects.requireNonNull(recovered.getJobParameters().getLong("fencingToken")), recovered);
        ledger.bind(link);
        return link;
    }

    private JobParameters parameters(BatchApprovedLaunchRequest request, String cpfExecutionId) {
        JobParametersBuilder builder = new JobParametersBuilder()
                .addString("cpfExecutionId", cpfExecutionId, true)
                .addString("jobId", request.definition().jobId(), true)
                .addLong("definitionVersion", request.definition().definitionVersion(), true)
                .addString("approvalId", request.approvalId(), true)
                .addString("operatorId", request.operatorId(), false)
                .addString("reason", request.reason(), false)
                .addString("idempotencyKey", request.idempotencyKey(), true)
                .addLong("fencingToken", request.fencingToken(), true)
                .addString("planChecksum", request.plan().checksum(), true)
                .addString("definitionChecksum", request.definition().checksum(), true)
                .addString("jobName", request.definition().jobName(), false)
                .addString("ownerDomain", request.definition().ownerDomain(), false)
                .addString("executorReference", request.definition().executorReference(), false)
                .addLong("timeoutSeconds", request.definition().resourcePolicy().timeoutSeconds(), false);
        request.parameters().forEach((name, value) -> add(builder, name, value));
        return builder.toJobParameters();
    }

    private static void add(JobParametersBuilder builder, String name, Object value) {
        if (value == null) return;
        if (value instanceof Long v) builder.addLong(name, v, false);
        else if (value instanceof Integer v) builder.addLong(name, v.longValue(), false);
        else if (value instanceof Double v) builder.addDouble(name, v, false);
        else if (value instanceof java.time.LocalDate v) builder.addLocalDate(name, v, false);
        else if (value instanceof java.time.LocalDateTime v) builder.addLocalDateTime(name, v, false);
        else builder.addString(name, Objects.toString(value), false);
    }

    private JobExecution required(long id) {
        JobExecution execution = repository.getJobExecution(id);
        if (execution == null) throw new IllegalArgumentException("JOB_EXECUTION_NOT_FOUND:" + id);
        return execution;
    }

    private static BatchExecutionLink link(
            String cpfExecutionId, String jobId, long version, long fencingToken, JobExecution execution) {
        return new BatchExecutionLink(
                cpfExecutionId, jobId, version,
                execution.getJobInstance().getInstanceId(), execution.getId(), null,
                execution.getStatus().name(), fencingToken, Instant.now());
    }

    private static void requireOperator(String operatorId, String reason) {
        if (operatorId == null || operatorId.isBlank()) throw new IllegalArgumentException("operatorId is required");
        if (reason == null || reason.trim().length() < 5) throw new IllegalArgumentException("reason must be at least 5 characters");
    }
}
