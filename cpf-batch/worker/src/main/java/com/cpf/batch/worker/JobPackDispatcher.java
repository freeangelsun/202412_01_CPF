package com.cpf.batch.worker;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchParameterDefinition;
import com.cpf.batch.runtime.JobPackCatalog;
import com.cpf.batch.runtime.LogContext;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.FileProcessHandler;
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
    private BatchFileProcessHandlerRegistry fileProcessHandlers;

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


    @Autowired(required = false)
    void setFileProcessHandlers(BatchFileProcessHandlerRegistry fileProcessHandlers) {
        this.fileProcessHandlers = fileProcessHandlers;
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
                        lease, work, outcome.status(), outcome.message(), outcome.attemptDetail())) {
                    throw new IllegalStateException(
                            "Worker retry requeue rejected because its lease expired or was fenced");
                }
                executionStatePersisted = true;
                leases.complete(lease, outcome.status(), outcome.message());
                return;
            }

            if (!executions.completeAttempt(
                    lease, work, outcome.status(), outcome.message(), outcome.attemptDetail())) {
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
                    if (executions.requeueAttempt(lease, work, status, failureMessage, JdbcWorkerExecutionRepository.AttemptDetail.empty())) {
                        leases.complete(lease, status, failureMessage);
                        return;
                    }
                } else if (executions.completeAttempt(lease, work, status, failureMessage, JdbcWorkerExecutionRepository.AttemptDetail.empty())) {
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
            case FILE_PROCESS -> executeFileProcess(definition, parameters, work, lease);
            case FILE_WATCH -> executeFileWatch(definition, parameters);
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
                yield new ExecutionOutcome(status, message,
                        new JdbcWorkerExecutionRepository.AttemptDetail(
                                definition.executorType().name(), null, null, null, false,
                                null, null, result.unknownResult()));
            }
        };
    }

    private ExecutionOutcome executeFileProcess(
            BatchJobDefinition definition,
            Map<String, Object> parameters,
            JdbcWorkerExecutionRepository.Work work,
            JdbcWorkerLeaseRepository.Lease lease) throws Exception {
        if (fileProcessHandlers == null) {
            throw new IllegalStateException("FILE_PROCESS handler registry is not installed");
        }
        String processorId = definition.processorId();
        String requestedProcessorId = optional(parameters, "processorId");
        if (requestedProcessorId != null && !processorId.equalsIgnoreCase(requestedProcessorId)) {
            throw new SecurityException(
                    "Runtime processorId cannot override published FILE_PROCESS definition");
        }
        FileProcessHandler handler = fileProcessHandlers.require(processorId);
        String sourceAlias = required(parameters, "sourceAlias");
        String sourcePath = required(parameters, "sourcePath");
        java.time.Duration timeout = java.time.Duration.ofSeconds(
                Math.max(1, definition.resourcePolicy().timeoutSeconds()));
        java.time.Duration stableWindow = java.time.Duration.ofSeconds(
                Math.max(1, optionalLong(parameters, "stableWindowSeconds", 2L)));
        Long expectedSize = optionalNullableLong(parameters, "expectedSize");
        String expectedSha256 = optional(parameters, "expectedSha256");
        String markerSuffix = optional(parameters, "completionMarkerSuffix");

        java.nio.file.Path ready = fileExecutor.awaitReady(new ApprovedFileExecutor.FileWatchRequest(
                sourceAlias, sourcePath, timeout, stableWindow, markerSuffix,
                expectedSize, expectedSha256));
        ApprovedFileExecutor.FileFingerprint before = fileExecutor.fingerprint(ready);
        String processingAlias = optional(parameters, "processingAlias");
        String completedAlias = optional(parameters, "completedAlias");
        String failedAlias = optional(parameters, "failedAlias");
        if (processingAlias != null && (completedAlias == null || failedAlias == null)) {
            throw new IllegalArgumentException(
                    "FILE_PROCESS processingAlias requires completedAlias and failedAlias");
        }
        ApprovedFileExecutor.Claim leaseClaim = null;
        java.nio.file.Path claimedPath;
        long fileFencingToken;

        if (processingAlias != null) {
            claimedPath = fileExecutor.claimForProcess(sourceAlias, sourcePath, processingAlias);
            fileFencingToken = lease.fencingToken();
        } else {
            if (!fileExecutor.sharedDurable(sourceAlias)) {
                throw new IllegalStateException(
                        "FILE_PROCESS requires processingAlias or a shared durable source alias");
            }
            leaseClaim = fileExecutor.claim(
                    sourceAlias, sourcePath,
                    "batch:" + work.executionId() + ":" + lease.fencingToken(),
                    timeout.plusSeconds(30));
            claimedPath = leaseClaim.path();
            fileFencingToken = leaseClaim.fencingToken();
        }

        try {
            ApprovedFileExecutor.FileFingerprint claimed = fileExecutor.fingerprint(claimedPath);
            if (!before.sha256().equalsIgnoreCase(claimed.sha256()) || before.size() != claimed.size()) {
                throw new java.io.IOException("Claimed file changed after readiness validation");
            }
            FileProcessHandler.FileProcessResult processed = handler.process(
                    new FileProcessHandler.FileProcessCommand(
                            work.executionId(), work.definitionVersion(), work.definitionChecksum(),
                            work.transactionId(), work.segmentId(), fileFencingToken, claimedPath,
                            claimed.size(), claimed.sha256(), parameters));
            if (processed == null) {
                throw new IllegalStateException("FILE_PROCESS handler returned null: " + processorId);
            }
            String status = processed.status().name();
            boolean completed = processed.status() == FileProcessHandler.Status.COMPLETED;
            String lifecycleAlias = completed ? completedAlias : failedAlias;
            if (processingAlias != null && lifecycleAlias != null) {
                fileExecutor.claimForProcess(processingAlias, sourcePath, lifecycleAlias);
            }
            String outputHash = processed.outputHash().isBlank() ? claimed.sha256() : processed.outputHash();
            String message = "processor=" + processorId
                    + ",file=" + claimed.fileName()
                    + ",size=" + claimed.size()
                    + ",sha256=" + claimed.sha256()
                    + ",fencingToken=" + fileFencingToken
                    + (processed.message().isBlank() ? "" : ",detail=" + processed.message());
            return new ExecutionOutcome(status, message,
                    new JdbcWorkerExecutionRepository.AttemptDetail(
                            BatchJobDefinition.ExecutorType.FILE_PROCESS.name(), null, null, null,
                            false, null, outputHash,
                            processed.status() == FileProcessHandler.Status.UNKNOWN_RESULT));
        } catch (Exception failure) {
            if (processingAlias != null && failedAlias != null) {
                try {
                    fileExecutor.claimForProcess(processingAlias, sourcePath, failedAlias);
                } catch (Exception recoveryFailure) {
                    failure.addSuppressed(recoveryFailure);
                }
            }
            throw failure;
        } finally {
            if (leaseClaim != null) {
                fileExecutor.release(leaseClaim);
            }
        }
    }

    private ExecutionOutcome executeFileWatch(
            BatchJobDefinition definition,
            Map<String, Object> parameters) throws Exception {
        String watchAlias = required(parameters, "watchAlias");
        String watchPath = required(parameters, "watchPath");
        if (Boolean.parseBoolean(Objects.toString(parameters.get("restartScan"), "false"))) {
            String directory = Objects.toString(parameters.get("scanDirectory"), ".");
            boolean recovered = fileExecutor.restartScan(watchAlias, directory).stream()
                    .anyMatch(path -> path.getFileName().toString()
                            .equals(java.nio.file.Path.of(watchPath).getFileName().toString()));
            if (!recovered) {
                throw new java.io.FileNotFoundException(
                        "Restart scan did not find the requested file: " + watchPath);
            }
        }
        java.time.Duration timeout = java.time.Duration.ofSeconds(
                Math.max(1, definition.resourcePolicy().timeoutSeconds()));
        java.time.Duration stableWindow = java.time.Duration.ofSeconds(
                Math.max(1, optionalLong(parameters, "stableWindowSeconds", 2L)));
        java.nio.file.Path ready = fileExecutor.awaitReady(new ApprovedFileExecutor.FileWatchRequest(
                watchAlias,
                watchPath,
                timeout,
                stableWindow,
                optional(parameters, "completionMarkerSuffix"),
                optionalNullableLong(parameters, "expectedSize"),
                optional(parameters, "expectedSha256")));
        ApprovedFileExecutor.FileFingerprint fingerprint = fileExecutor.fingerprint(ready);
        return new ExecutionOutcome("COMPLETED",
                "file=" + fingerprint.fileName() + ",size=" + fingerprint.size()
                        + ",sha256=" + fingerprint.sha256(),
                new JdbcWorkerExecutionRepository.AttemptDetail(
                        BatchJobDefinition.ExecutorType.FILE_WATCH.name(), null, null, null,
                        false, null, fingerprint.sha256(), false));
    }

    private static String optional(Map<String, Object> raw, String key) {
        String value = Objects.toString(raw.get(key), "").trim();
        return value.isEmpty() ? null : value;
    }

    private static long optionalLong(Map<String, Object> raw, String key, long defaultValue) {
        String value = optional(raw, key);
        return value == null ? defaultValue : Long.parseLong(value);
    }

    private static Long optionalNullableLong(Map<String, Object> raw, String key) {
        String value = optional(raw, key);
        return value == null ? null : Long.valueOf(value);
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
        return new ExecutionOutcome(status, message, JdbcWorkerExecutionRepository.AttemptDetail.empty());
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
        return new ExecutionOutcome(status, result.output(),
                new JdbcWorkerExecutionRepository.AttemptDetail(
                        BatchJobDefinition.ExecutorType.APPROVED_SHELL.name(),
                        result.exitCode(), result.stdout(), result.stderr(), result.truncated(),
                        result.durationMs(), result.artifactHash(), result.unknownResult()));
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

    private record ExecutionOutcome(
            String status, String message, JdbcWorkerExecutionRepository.AttemptDetail attemptDetail) {
        private ExecutionOutcome {
            status = Objects.requireNonNull(status, "status");
            message = SensitiveTextSanitizer.sanitize(message);
            attemptDetail = attemptDetail == null
                    ? JdbcWorkerExecutionRepository.AttemptDetail.empty() : attemptDetail;
        }

        static ExecutionOutcome completed() {
            return new ExecutionOutcome("COMPLETED", null, JdbcWorkerExecutionRepository.AttemptDetail.empty());
        }
    }
}
