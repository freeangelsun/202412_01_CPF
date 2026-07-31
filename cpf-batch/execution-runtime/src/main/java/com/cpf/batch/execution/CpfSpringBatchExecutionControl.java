package com.cpf.batch.execution;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchCanonicalDigest;
import com.cpf.batch.api.BatchControlState;
import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionReservation;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;

/** CPF Batch Control Plane을 Spring Batch JobOperator 생명주기에 연결하는 상태기계입니다. */
public final class CpfSpringBatchExecutionControl implements BatchExecutionControlPort {
    private final JobOperator operator;
    private final JobRepository repository;
    private final JobExplorer explorer;
    private final CpfBatchJobFactory jobs;
    private final BatchExecutionLedgerPort ledger;
    private final BatchFencingPort fencing;

    public CpfSpringBatchExecutionControl(
            JobOperator operator,
            JobRepository repository,
            JobExplorer explorer,
            CpfBatchJobFactory jobs,
            BatchExecutionLedgerPort ledger,
            BatchFencingPort fencing) {
        this.operator = operator;
        this.repository = repository;
        this.explorer = explorer;
        this.jobs = jobs;
        this.ledger = ledger;
        this.fencing = fencing;
    }

    @Override
    public BatchExecutionLink start(BatchApprovedLaunchRequest request) {
        request.plan().verifyIntegrity();
        String cpfExecutionId = ledger.reserve(request);
        BatchExecutionReservation reservation = requiredReservation(cpfExecutionId);
        if (reservation.state() != BatchControlState.RESERVED) {
            return reconcile(cpfExecutionId);
        }

        Job job;
        try {
            job = jobs.materialize(request.plan());
        } catch (RuntimeException invalidDefinition) {
            ledger.transition(cpfExecutionId, Set.of(BatchControlState.RESERVED), BatchControlState.REJECTED,
                    "BATCH_PLAN_MATERIALIZATION_REJECTED", invalidDefinition.getMessage(), null);
            throw invalidDefinition;
        }

        fencing.assertCurrent(request.definition().jobId(), cpfExecutionId, request.fencingToken());
        ledger.transition(cpfExecutionId, Set.of(BatchControlState.RESERVED), BatchControlState.STARTING,
                null, null, null);
        try {
            JobExecution execution = operator.start(job, parameters(request, cpfExecutionId));
            BatchExecutionLink link = link(cpfExecutionId, request.definition().jobId(),
                    request.definition().definitionVersion(), request.fencingToken(), execution);
            ledger.bind(link);
            return link;
        } catch (RuntimeException failure) {
            ledger.recordUnknown(cpfExecutionId, "BATCH_START_RESPONSE_UNKNOWN", safe(failure));
            throw new CpfBatchUnknownResultException(
                    "BATCH_START_RESPONSE_UNKNOWN",
                    "Spring Batch start outcome is unknown. Reconcile with cpfExecutionId=" + cpfExecutionId);
        }
    }

    @Override
    public boolean stop(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        JobExecution execution = required(jobExecutionId);
        String cpfExecutionId = required(execution, "cpfExecutionId");
        ledger.transition(cpfExecutionId,
                Set.of(BatchControlState.STARTING, BatchControlState.STARTED, BatchControlState.UNKNOWN_RESULT),
                BatchControlState.STOPPING, "OPERATOR_STOP", reason, null);
        try {
            boolean accepted = operator.stop(execution);
            if (!accepted) {
                ledger.recordUnknown(cpfExecutionId, "BATCH_STOP_NOT_ACCEPTED", "JobOperator.stop returned false");
            }
            return accepted;
        } catch (RuntimeException failure) {
            ledger.recordUnknown(cpfExecutionId, "BATCH_STOP_RESPONSE_UNKNOWN", safe(failure));
            throw new CpfBatchUnknownResultException(
                    "BATCH_STOP_RESPONSE_UNKNOWN", "Stop outcome is unknown for " + cpfExecutionId);
        }
    }

    @Override
    public BatchExecutionLink restart(long jobExecutionId, String operatorId, String reason, long fencingToken) {
        requireOperator(operatorId, reason);
        JobExecution previous = required(jobExecutionId);
        String cpfExecutionId = required(previous, "cpfExecutionId");
        String jobId = required(previous, "jobId");
        fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);
        try {
            JobExecution restarted = operator.restart(previous);
            BatchExecutionLink link = link(cpfExecutionId, jobId,
                    requiredLong(previous, "definitionVersion"), fencingToken, restarted);
            ledger.bind(link);
            return link;
        } catch (RuntimeException failure) {
            ledger.recordUnknown(cpfExecutionId, "BATCH_RESTART_RESPONSE_UNKNOWN", safe(failure));
            throw new CpfBatchUnknownResultException(
                    "BATCH_RESTART_RESPONSE_UNKNOWN", "Restart outcome is unknown for " + cpfExecutionId);
        }
    }

    @Override
    public void abandon(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        JobExecution execution = required(jobExecutionId);
        String cpfExecutionId = required(execution, "cpfExecutionId");
        operator.abandon(execution);
        ledger.transition(cpfExecutionId,
                Set.of(BatchControlState.STOPPED, BatchControlState.FAILED, BatchControlState.UNKNOWN_RESULT),
                BatchControlState.ABANDONED, "OPERATOR_ABANDON", reason, null);
    }

    @Override
    public BatchExecutionLink reconcile(String cpfExecutionId) {
        BatchExecutionReservation reservation = requiredReservation(cpfExecutionId);
        Optional<BatchExecutionLink> linked = ledger.findByCpfExecutionId(cpfExecutionId).stream()
                .filter(link -> link.stepExecutionId() == null && link.jobExecutionId() != null)
                .max(Comparator.comparing(BatchExecutionLink::observedAt));
        if (linked.isPresent()) {
            JobExecution execution = required(linked.get().jobExecutionId());
            BatchExecutionLink observed = link(cpfExecutionId, reservation.jobId(), reservation.definitionVersion(),
                    reservation.fencingToken(), execution);
            ledger.bind(observed);
            return observed;
        }

        String jobName = CpfBatchJobFactory.jobName(
                reservation.jobId(), reservation.definitionVersion(), reservation.planChecksum());
        Optional<JobExecution> recovered = explorer.getJobInstances(jobName, 0, 100).stream()
                .flatMap(instance -> explorer.getJobExecutions(instance).stream())
                .filter(execution -> cpfExecutionId.equals(execution.getJobParameters().getString("cpfExecutionId")))
                .max(Comparator.comparing(JobExecution::getId));
        if (recovered.isPresent()) {
            BatchExecutionLink observed = link(cpfExecutionId, reservation.jobId(), reservation.definitionVersion(),
                    reservation.fencingToken(), recovered.get());
            ledger.bind(observed);
            return observed;
        }

        ledger.recordUnknown(cpfExecutionId, "BATCH_RECONCILE_NOT_FOUND",
                "No Spring Batch metadata matched the CPF reservation");
        throw new CpfBatchUnknownResultException(
                "BATCH_RECONCILE_NOT_FOUND", "No Spring Batch execution found for " + cpfExecutionId);
    }

    public BatchExecutionLink recover(long jobExecutionId, String operatorId, String reason) {
        requireOperator(operatorId, reason);
        JobExecution recovered = operator.recover(required(jobExecutionId));
        String cpfExecutionId = required(recovered, "cpfExecutionId");
        BatchExecutionLink link = link(cpfExecutionId, required(recovered, "jobId"),
                requiredLong(recovered, "definitionVersion"), requiredLong(recovered, "fencingToken"), recovered);
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
                .addString("idempotencyScope", request.idempotencyScope(), true)
                .addString("idempotencyKey", request.idempotencyKey(), true)
                .addString("requestHash", request.requestHash(), true)
                .addLong("fencingToken", request.fencingToken(), true)
                .addString("planChecksum", request.plan().checksum(), true)
                .addString("definitionChecksum", request.definition().checksum(), true)
                .addString("parameterDigest", BatchCanonicalDigest.sha256(request.parameters()), true)
                .addString("jobName", request.definition().jobName(), false)
                .addString("ownerDomain", request.definition().ownerDomain(), false)
                .addString("executorReference", request.definition().executorReference(), false)
                .addLong("timeoutSeconds", request.definition().resourcePolicy().timeoutSeconds(), false);
        request.parameters().forEach((name, value) -> add(builder, "arg." + name, value));
        return builder.toJobParameters();
    }

    private static void add(JobParametersBuilder builder, String name, Object value) {
        if (value == null) return;
        if (value instanceof Long v) builder.addLong(name, v, false);
        else if (value instanceof Integer v) builder.addLong(name, v.longValue(), false);
        else if (value instanceof Double v) builder.addDouble(name, v, false);
        else if (value instanceof java.time.LocalDate v) builder.addLocalDate(name, v, false);
        else if (value instanceof java.time.LocalDateTime v) builder.addLocalDateTime(name, v, false);
        else if (value instanceof String v) builder.addString(name, v, false);
        else builder.addString(name, BatchCanonicalDigest.canonicalText(value), false);
    }

    private BatchExecutionReservation requiredReservation(String cpfExecutionId) {
        return ledger.findReservation(cpfExecutionId)
                .orElseThrow(() -> new IllegalArgumentException("CPF_EXECUTION_NOT_FOUND:" + cpfExecutionId));
    }

    private JobExecution required(long id) {
        JobExecution execution = repository.getJobExecution(id);
        if (execution == null) throw new IllegalArgumentException("JOB_EXECUTION_NOT_FOUND:" + id);
        return execution;
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

    private static BatchExecutionLink link(
            String cpfExecutionId, String jobId, long version, long fencingToken, JobExecution execution) {
        JobInstance instance = execution.getJobInstance();
        return new BatchExecutionLink(
                cpfExecutionId, jobId, version,
                instance == null ? null : instance.getInstanceId(), execution.getId(), null,
                execution.getStatus().name(), fencingToken, Instant.now());
    }

    private static void requireOperator(String operatorId, String reason) {
        if (operatorId == null || operatorId.isBlank()) throw new IllegalArgumentException("operatorId is required");
        if (reason == null || reason.trim().length() < 5) {
            throw new IllegalArgumentException("reason must be at least 5 characters");
        }
    }

    private static String safe(Throwable failure) {
        String text = failure == null ? "" : Objects.toString(failure.getMessage(), failure.getClass().getSimpleName());
        text = text.replaceAll("(?i)(password|token|secret|authorization|cookie|session(?:id)?)\\s*[=:]\\s*[^,;\\s]+", "$1=<masked>")
                .replaceAll("[\\r\\n\\t]+", " ").trim();
        return text.length() <= 2_000 ? text : text.substring(0, 2_000);
    }
}
