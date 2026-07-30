package com.cpf.batch.worker;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchParameterDefinition;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.runtime.LogContext;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.worker.internal.JdbcWorkerExecutionRepository;
import com.cpf.batch.worker.internal.JdbcWorkerLeaseRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeoutException;

/**
 * Published Job Definition의 고정 Version/Checksum을 실행 시점에 다시 검증하고,
 * Job Pack/Shell/File Executor를 동일 Lease·Fencing·Attempt 원장 경계에서 실행합니다.
 */
@Component
public class JobPackDispatcher {
    private final JobPackCatalog catalog;
    private final JobOperator jobOperator;
    private final JdbcWorkerExecutionRepository executions;
    private final JdbcWorkerLeaseRepository leases;
    private final ObjectMapper objectMapper;
    private final ApprovedShellExecutor shellExecutor;
    private final ApprovedFileExecutor fileExecutor;
    private BatchRuntimeExecutorRegistry runtimeExecutorRegistry;

    public JobPackDispatcher(
            JobPackCatalog catalog,
            JobOperator jobOperator,
            JdbcWorkerExecutionRepository executions,
            JdbcWorkerLeaseRepository leases,
            ObjectMapper objectMapper,
            ApprovedShellExecutor shellExecutor,
            ApprovedFileExecutor fileExecutor) {
        this.catalog = catalog;
        this.jobOperator = jobOperator;
        this.executions = executions;
        this.leases = leases;
        this.objectMapper = objectMapper;
        this.shellExecutor = shellExecutor;
        this.fileExecutor = fileExecutor;
    }

    /**
     * 외부 Executor Registry는 선택 Capability가 아니라, 해당 Executor type을 Publish하는 경우 필수입니다.
     * 생성자 Source Compatibility는 유지하고 Spring Runtime에서만 명시적으로 주입합니다.
     */
    @Autowired(required = false)
    void setRuntimeExecutorRegistry(BatchRuntimeExecutorRegistry runtimeExecutorRegistry) {
        this.runtimeExecutorRegistry = runtimeExecutorRegistry;
    }

    public void execute(JdbcWorkerLeaseRepository.Lease lease) {
        JdbcWorkerExecutionRepository.Work work = executions.load(lease.executionId());
        BatchJobDefinition runtimeDefinition = decodeAndValidateDefinition(work);
        boolean executionStatePersisted = false;

        try (LogContext ignored = LogContext.open(Map.of(
                "transactionId", Objects.toString(work.transactionId(), ""),
                "segmentId", Objects.toString(work.segmentId(), ""),
                "executionId", Long.toString(work.executionId()),
                "jobId", work.jobId(),
                "definitionVersion", Long.toString(work.definitionVersion()),
                "definitionChecksum", work.definitionChecksum()))) {
            if (!executions.startAttempt(lease, work)) {
                throw new IllegalStateException(
                        "Worker lease expired or was fenced before business execution");
            }

            Map<String, Object> validatedParameters = validateParameters(
                    runtimeDefinition, parse(work.parametersJson()));
            ExecutionOutcome outcome = executeDefinition(
                    runtimeDefinition, validatedParameters, work, lease);

            if (shouldRetry(runtimeDefinition, work, outcome.status())) {
                if (!executions.requeueAttempt(
                        lease, work, outcome.status(), outcome.message())) {
                    throw new IllegalStateException(
                            "Worker retry requeue rejected because its lease expired or was fenced");
                }
                executionStatePersisted = true;
                leases.complete(lease, outcome.status(), outcome.message());
                return;
            }

            if (!executions.completeAttempt(
                    lease, work, outcome.status(), outcome.message())) {
                throw new IllegalStateException(
                        "Worker completion rejected because its lease expired or was fenced");
            }
            executionStatePersisted = true;
            leases.complete(lease, outcome.status(), outcome.message());
        } catch (Exception failure) {
            String failureMessage = SensitiveTextSanitizer.sanitize(failure.getMessage());
            if (executionStatePersisted) {
                throw new IllegalStateException(
                        "Batch result was persisted but lease finalization is unresolved; recovery is required. "
                                + failureMessage,
                        failure);
            }

            String status = classifyFailure(failure);
            RuntimeException finalizationFailure = null;
            try {
                if (shouldRetry(runtimeDefinition, work, status)) {
                    if (executions.requeueAttempt(lease, work, status, failureMessage)) {
                        leases.complete(lease, status, failureMessage);
                        return;
                    }
                } else if (executions.completeAttempt(lease, work, status, failureMessage)) {
                    leases.complete(lease, status, failureMessage);
                }
            } catch (RuntimeException persistenceFailure) {
                finalizationFailure = persistenceFailure;
            }

            if (finalizationFailure != null) {
                failure.addSuppressed(finalizationFailure);
                throw new IllegalStateException(
                        "Batch execution failed and result persistence is unresolved; recovery is required. "
                                + SensitiveTextSanitizer.sanitize(finalizationFailure.getMessage()),
                        failure);
            }
            throw new IllegalStateException(
                    "Batch execution " + status + ": " + failureMessage, failure);
        }
    }

    private ExecutionOutcome executeDefinition(
            BatchJobDefinition definition,
            Map<String, Object> parameters,
            JdbcWorkerExecutionRepository.Work work,
            JdbcWorkerLeaseRepository.Lease lease) throws Exception {
        return switch (definition.executorType()) {
            case SPRING_BATCH -> executeSpringBatch(definition, parameters, work, lease);
            case APPROVED_SHELL -> executeShell(definition, parameters);
            case FILE_TRANSFER -> {
                fileExecutor.transfer(
                        required(parameters, "sourceAlias"),
                        required(parameters, "sourcePath"),
                        required(parameters, "targetAlias"),
                        required(parameters, "targetPath"),
                        Boolean.parseBoolean(Objects.toString(parameters.get("overwrite"), "false")),
                        work.transactionId(), work.segmentId(), "cpf-batch-worker");
                yield ExecutionOutcome.completed();
            }
            case FILE_PROCESS -> {
                fileExecutor.claimForProcess(
                        required(parameters, "sourceAlias"),
                        required(parameters, "sourcePath"),
                        required(parameters, "processingAlias"));
                yield ExecutionOutcome.completed();
            }
            case FILE_WATCH -> {
                fileExecutor.await(
                        required(parameters, "watchAlias"),
                        required(parameters, "watchPath"),
                        java.time.Duration.ofSeconds(definition.resourcePolicy().timeoutSeconds()));
                yield ExecutionOutcome.completed();
            }
            case SERVICE_CALL, MESSAGE_TRIGGER, PROTOCOL_ADAPTER -> {
                if (runtimeExecutorRegistry == null) {
                    throw new IllegalStateException(
                            "Required Batch Runtime Executor Registry is not installed: "
                                    + definition.executorType());
                }
                BatchRuntimeExecutorRegistry.ExecutionResult result = runtimeExecutorRegistry.execute(
                        definition, parameters, work.executionId(), work.transactionId(), work.segmentId());
                String status = result.unknownResult()
                        ? "UNKNOWN_RESULT"
                        : result.status().toUpperCase(Locale.ROOT);
                String message = result.code() + ": " + result.message();
                yield new ExecutionOutcome(status, message);
            }
        };
    }

    private ExecutionOutcome executeSpringBatch(
            BatchJobDefinition definition,
            Map<String, Object> parameters,
            JdbcWorkerExecutionRepository.Work work,
            JdbcWorkerLeaseRepository.Lease lease) throws Exception {
        var provider = catalog.providerFor(work.jobId());
        Object resolved = provider.resolveJob(work.jobId());
        if (!(resolved instanceof Job job)) {
            throw new IllegalStateException("Job Pack returned non-Spring Batch Job");
        }
        JobParameters jobParameters = buildParameters(definition, parameters, work, lease);
        JobExecution launched = jobOperator.start(job, jobParameters);
        if (!executions.recordSpringExecution(
                lease,
                launched.getId(),
                launched.getJobInstance() == null ? null : launched.getJobInstance().getId())) {
            throw new IllegalStateException(
                    "Worker lease expired or was fenced while the Spring Batch Job was running");
        }
        String status = switch (launched.getStatus()) {
            case COMPLETED -> "COMPLETED";
            case FAILED, ABANDONED -> "FAILED";
            case STOPPED -> "STOPPED";
            default -> "UNKNOWN_RESULT";
        };
        String message = launched.getAllFailureExceptions().stream()
                .map(Throwable::getMessage)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
        return new ExecutionOutcome(status, message);
    }

    private ExecutionOutcome executeShell(
            BatchJobDefinition definition, Map<String, Object> parameters) throws Exception {
        String scriptKey = definition.executorReference().substring("SCRIPT:".length()).trim();
        if (scriptKey.isEmpty()) {
            throw new SecurityException("Approved script reference is empty");
        }
        ApprovedShellExecutor.Result result = shellExecutor.execute(scriptKey, parameters);
        String status;
        if (result.unknownResult()) {
            status = "UNKNOWN_RESULT";
        } else {
            status = switch (result.status().toUpperCase(Locale.ROOT)) {
                case "SUCCESS" -> "COMPLETED";
                case "TIMEOUT" -> "TIMEOUT";
                case "RETRYABLE_FAILURE" -> "RETRYABLE_FAILURE";
                case "STOPPED" -> "STOPPED";
                case "UNKNOWN_RESULT" -> "UNKNOWN_RESULT";
                default -> result.success() ? "COMPLETED" : "FAILED";
            };
        }
        return new ExecutionOutcome(status, result.output());
    }

    private BatchJobDefinition decodeAndValidateDefinition(
            JdbcWorkerExecutionRepository.Work work) {
        if (work.definitionVersion() <= 0
                || work.definitionChecksum() == null
                || work.definitionChecksum().isBlank()) {
            throw new IllegalStateException(
                    "Execution is missing fixed Definition Version/Checksum: " + work.executionId());
        }
        try {
            BatchJobDefinition definition = objectMapper.readValue(
                    work.definitionJson(), BatchJobDefinition.class);
            if (!work.jobId().equals(definition.jobId())) {
                throw new IllegalStateException("Definition jobId mismatch");
            }
            if (work.definitionVersion() != definition.definitionVersion()) {
                throw new IllegalStateException("Definition version mismatch");
            }
            if (!work.definitionChecksum().equals(definition.checksum())) {
                throw new IllegalStateException("Definition checksum mismatch");
            }
            if (!definition.executorType().name().equals(work.executorType())) {
                throw new IllegalStateException("Definition executor type mismatch");
            }
            if (!definition.executorReference().equals(work.executorReference())) {
                throw new IllegalStateException("Definition executor reference mismatch");
            }
            return definition;
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IllegalStateException("Published Definition JSON decode failed", failure);
        }
    }

    private Map<String, Object> validateParameters(
            BatchJobDefinition definition, Map<String, Object> supplied) {
        Set<String> declared = definition.parameters().stream()
                .map(BatchParameterDefinition::name)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
        for (String name : supplied.keySet()) {
            if (!declared.contains(name)) {
                throw new IllegalArgumentException("Unknown batch parameter: " + name);
            }
        }

        Map<String, Object> validated = new LinkedHashMap<>();
        for (BatchParameterDefinition parameter : definition.parameters()) {
            Object raw = supplied.get(parameter.name());
            String suppliedValue = raw == null ? null : Objects.toString(raw, null);
            if (supplied.containsKey(parameter.name())
                    && !parameter.overrideAllowed()
                    && parameter.defaultValue() != null
                    && !Objects.equals(parameter.defaultValue(), suppliedValue)) {
                throw new SecurityException(
                        "Runtime override is prohibited for parameter: " + parameter.name());
            }
            BatchParameterDefinition.ValidationResult result = parameter.validate(suppliedValue);
            if (!result.valid()) {
                throw new IllegalArgumentException(
                        "Invalid batch parameter " + parameter.name() + " [" + result.code() + "]: "
                                + result.message());
            }
            String effective = parameter.effectiveValue(suppliedValue);
            if (effective != null && !effective.isBlank()) {
                validated.put(parameter.name(), effective);
            }
        }
        return Map.copyOf(validated);
    }

    private JobParameters buildParameters(
            BatchJobDefinition definition,
            Map<String, Object> validated,
            JdbcWorkerExecutionRepository.Work work,
            JdbcWorkerLeaseRepository.Lease lease) {
        JobParametersBuilder builder = new JobParametersBuilder();
        for (BatchParameterDefinition parameter : definition.parameters()) {
            Object value = validated.get(parameter.name());
            if (value == null) {
                continue;
            }
            add(builder, parameter, String.valueOf(value));
        }
        builder.addLong("cpfExecutionId", work.executionId(), true);
        builder.addLong("cpfDefinitionVersion", work.definitionVersion(), true);
        builder.addString("cpfDefinitionChecksum", work.definitionChecksum(), true);
        builder.addLong("cpfFencingToken", lease.fencingToken(), true);
        if (work.businessDate() != null) {
            builder.addLocalDate("businessDate", work.businessDate(), true);
        }
        return builder.toJobParameters();
    }

    private Map<String, Object> parse(String json) throws Exception {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        return objectMapper.readValue(json, new TypeReference<>() {});
    }

    private static String required(Map<String, Object> raw, String key) {
        String value = Objects.toString(raw.get(key), "").trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException(key + " is required");
        }
        return value;
    }

    private static boolean shouldRetry(
            BatchJobDefinition definition,
            JdbcWorkerExecutionRepository.Work work,
            String status) {
        if (!Set.of("RETRYABLE_FAILURE", "TIMEOUT").contains(status)) {
            return false;
        }
        return work.restartAttempt() + 1 < definition.recoveryPolicy().maxAttempts();
    }

    private static String classifyFailure(Exception failure) {
        if (failure instanceof TimeoutException) {
            return "TIMEOUT";
        }
        if (failure instanceof IOException) {
            return "RETRYABLE_FAILURE";
        }
        String message = Objects.toString(failure.getMessage(), "").toLowerCase(Locale.ROOT);
        if (message.contains("timeout")) {
            return "TIMEOUT";
        }
        if (message.contains("fenced")
                || message.contains("lease expired")
                || message.contains("unresolved")) {
            return "UNKNOWN_RESULT";
        }
        return "FAILED";
    }

    private static void add(
            JobParametersBuilder builder,
            BatchParameterDefinition parameter,
            String value) {
        boolean identifying = parameter.identifying() && !parameter.sensitive();
        switch (parameter.type()) {
            case "INTEGER", "LONG" -> builder.addLong(
                    parameter.name(), Long.parseLong(value), identifying);
            case "DATE" -> builder.addLocalDate(
                    parameter.name(), LocalDate.parse(value), identifying);
            case "DATETIME" -> builder.addLocalDateTime(
                    parameter.name(), OffsetDateTime.parse(value).toLocalDateTime(), identifying);
            default -> builder.addString(parameter.name(), value, identifying);
        }
    }

    private record ExecutionOutcome(String status, String message) {
        private ExecutionOutcome {
            status = Objects.requireNonNull(status, "status");
            message = SensitiveTextSanitizer.sanitize(message);
        }

        static ExecutionOutcome completed() {
            return new ExecutionOutcome("COMPLETED", null);
        }
    }
}
