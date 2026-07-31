package com.cpf.batch.worker;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.batch.spi.FileProcessHandler;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** 기존 보안 Executor를 Spring Batch Step 내부에서만 호출하도록 고정하는 Product Adapter입니다. */
@Component
public final class SpringBatchWorkerStepHandler implements BatchStepHandler {
    private final ApprovedShellExecutor shell;
    private final ApprovedFileExecutor files;
    private final BatchFileProcessHandlerRegistry fileHandlers;
    private final BatchRuntimeExecutorRegistry external;

    public SpringBatchWorkerStepHandler(
            ApprovedShellExecutor shell,
            ApprovedFileExecutor files,
            BatchFileProcessHandlerRegistry fileHandlers,
            BatchRuntimeExecutorRegistry external) {
        this.shell = shell;
        this.files = files;
        this.fileHandlers = fileHandlers;
        this.external = external;
    }

    @Override
    public boolean supports(BatchJobDefinition.ExecutorType type, String reference) {
        return switch (type) {
            case APPROVED_SHELL, FILE_PROCESS, FILE_TRANSFER,
                    SERVICE_CALL, MESSAGE_TRIGGER, PROTOCOL_ADAPTER -> true;
            case SPRING_BATCH, FILE_WATCH -> false;
        };
    }

    @Override
    public BatchStepResult execute(BatchStepCommand command) throws Exception {
        Map<String, Object> parameters = merged(command);
        return switch (command.step().executorType()) {
            case APPROVED_SHELL -> shell(command, parameters);
            case FILE_PROCESS -> fileProcess(command, parameters);
            case FILE_TRANSFER -> fileTransfer(command, parameters);
            case SERVICE_CALL, MESSAGE_TRIGGER, PROTOCOL_ADAPTER -> external(command, parameters);
            default -> throw new IllegalArgumentException(
                    "Unsupported Spring Batch worker executor: " + command.step().executorType());
        };
    }

    private BatchStepResult shell(BatchStepCommand command, Map<String, Object> parameters) throws Exception {
        ApprovedShellExecutor.Result result = shell.execute(command.step().executorReference(), parameters);
        Map<String, Object> checkpoint = Map.of(
                "shell.exitCode", result.exitCode(),
                "shell.durationMs", result.durationMs(),
                "shell.artifactHash", result.artifactHash(),
                "shell.outputTruncated", result.truncated(),
                "shell.stdout", result.stdout(),
                "shell.stderr", result.stderr());
        if (result.success()) {
            return BatchStepResult.completed(result.status(), 1, 1, checkpoint);
        }
        if (result.unknownResult()) {
            return new BatchStepResult(Status.UNKNOWN_RESULT, "SHELL_UNKNOWN_RESULT", result.status(),
                    1, 0, 0, checkpoint);
        }
        return new BatchStepResult(Status.FAILED, "SHELL_EXIT_" + result.exitCode(), result.status(),
                1, 0, 0, checkpoint);
    }

    private BatchStepResult fileProcess(BatchStepCommand command, Map<String, Object> parameters) throws Exception {
        String sourceAlias = required(parameters, "sourceAlias");
        String sourcePath = required(parameters, "sourcePath");
        String processingAlias = required(parameters, "processingAlias");
        String completedAlias = required(parameters, "completedAlias");
        String failedAlias = required(parameters, "failedAlias");
        String processorId = processorId(command.step().executorReference());
        Path claimedPath = files.claimForProcess(sourceAlias, sourcePath, processingAlias);
        ApprovedFileExecutor.FileFingerprint fingerprint = files.fingerprint(claimedPath);
        FileProcessHandler handler = fileHandlers.require(processorId);
        FileProcessHandler.FileProcessResult result;
        try {
            result = handler.process(new FileProcessHandler.FileProcessCommand(
                    command.jobExecutionId(),
                    longValue(parameters, "definitionVersion", 1L),
                    required(parameters, "definitionChecksum"),
                    Objects.toString(parameters.get("transactionId"), command.cpfExecutionId()),
                    Objects.toString(parameters.get("segmentId"), command.step().stepId()),
                    command.fencingToken(), claimedPath, fingerprint.size(), fingerprint.sha256(), parameters));
        } catch (Exception failure) {
            moveFromProcessing(processingAlias, sourcePath, failedAlias);
            throw failure;
        }
        Map<String, Object> checkpoint = Map.of(
                "file.name", fingerprint.fileName(),
                "file.size", fingerprint.size(),
                "file.sha256", fingerprint.sha256(),
                "file.outputHash", result.outputHash());
        return switch (result.status()) {
            case COMPLETED -> {
                moveFromProcessing(processingAlias, sourcePath, completedAlias);
                yield BatchStepResult.completed(result.message(), 1, 1, checkpoint);
            }
            case RETRYABLE_FAILURE -> {
                moveFromProcessing(processingAlias, sourcePath, failedAlias);
                yield new BatchStepResult(Status.RETRYABLE_FAILURE, "FILE_PROCESS_RETRYABLE",
                        result.message(), 1, 0, 0, checkpoint);
            }
            case FAILED -> {
                moveFromProcessing(processingAlias, sourcePath, failedAlias);
                yield new BatchStepResult(Status.FAILED, "FILE_PROCESS_FAILED",
                        result.message(), 1, 0, 0, checkpoint);
            }
            case UNKNOWN_RESULT -> new BatchStepResult(Status.UNKNOWN_RESULT, "FILE_PROCESS_UNKNOWN",
                    result.message(), 1, 0, 0, checkpoint);
        };
    }

    private BatchStepResult fileTransfer(BatchStepCommand command, Map<String, Object> parameters) throws Exception {
        Path path = files.transfer(
                required(parameters, "sourceAlias"), required(parameters, "sourcePath"),
                required(parameters, "targetAlias"), required(parameters, "targetPath"),
                Boolean.parseBoolean(Objects.toString(parameters.getOrDefault("overwrite", "false"))),
                Objects.toString(parameters.get("transactionId"), command.cpfExecutionId()),
                Objects.toString(parameters.get("segmentId"), command.step().stepId()),
                Objects.toString(parameters.get("operatorId"), "cpf-batch"));
        ApprovedFileExecutor.FileFingerprint fingerprint = files.fingerprint(path);
        return BatchStepResult.completed("FILE_TRANSFER_COMPLETED", 1, 1, Map.of(
                "file.path", path.toString(), "file.sha256", fingerprint.sha256(),
                "file.size", fingerprint.size()));
    }

    private BatchStepResult external(BatchStepCommand command, Map<String, Object> parameters) throws Exception {
        BatchJobDefinition definition = definition(command, parameters);
        BatchRuntimeExecutorRegistry.ExecutionResult result = external.execute(
                definition, parameters, command.jobExecutionId(),
                Objects.toString(parameters.get("transactionId"), command.cpfExecutionId()),
                Objects.toString(parameters.get("segmentId"), command.step().stepId()));
        Status status = result.unknownResult() ? Status.UNKNOWN_RESULT
                : "COMPLETED".equals(result.status()) ? Status.COMPLETED : Status.FAILED;
        return new BatchStepResult(status, result.code(), result.message(), 1,
                status == Status.COMPLETED ? 1 : 0, 0,
                Map.of("external.attemptCount", result.attemptCount()));
    }

    private void moveFromProcessing(String processingAlias, String path, String targetAlias) throws Exception {
        files.transfer(processingAlias, path, targetAlias, path, false);
        java.nio.file.Files.deleteIfExists(files.resolve(processingAlias, path));
    }

    private static Map<String, Object> merged(BatchStepCommand command) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>(command.jobParameters());
        values.putAll(command.step().parameters());
        values.put("cpfExecutionId", command.cpfExecutionId());
        values.put("jobExecutionId", command.jobExecutionId());
        values.put("stepExecutionId", command.stepExecutionId());
        values.put("fencingToken", command.fencingToken());
        return Map.copyOf(values);
    }

    private static BatchJobDefinition definition(BatchStepCommand command, Map<String, Object> p) {
        return new BatchJobDefinition(
                required(p, "jobId"), longValue(p, "definitionVersion", 1L),
                Objects.toString(p.getOrDefault("jobName", p.get("jobId"))),
                command.step().executorType(), BatchJobDefinition.State.PUBLISHED,
                Objects.toString(p.getOrDefault("ownerDomain", "BAT")),
                Objects.toString(p.getOrDefault("description", "Spring Batch dynamic step")),
                new BatchJobDefinition.Trigger(BatchJobDefinition.TriggerType.MANUAL, "", "Asia/Seoul",
                        BatchJobDefinition.MisfirePolicy.FAIL_CLOSED, true),
                java.util.List.of(), java.util.List.of(),
                new BatchJobDefinition.ResourcePolicy("DEFAULT", "", 1,
                        longValue(p, "timeoutSeconds", 3600L), 0, 0),
                BatchJobDefinition.RecoveryPolicy.defaults(), BatchJobDefinition.AlertPolicy.defaults(),
                command.step().executorReference(), Objects.toString(p.getOrDefault("definitionChecksum",
                        "0000000000000000000000000000000000000000000000000000000000000000")),
                Objects.toString(p.getOrDefault("operatorId", "cpf-batch")),
                Objects.toString(p.getOrDefault("reason", "approved Spring Batch execution")),
                null, null, 0L);
    }

    private static String processorId(String reference) {
        if (reference == null || !reference.startsWith("PROCESSOR:")) {
            throw new IllegalArgumentException("FILE_PROCESS requires PROCESSOR:<id>");
        }
        return reference.substring("PROCESSOR:".length());
    }
    private static String required(Map<String, Object> values, String name) {
        String value = Objects.toString(values.get(name), "").trim();
        if (value.isEmpty()) throw new IllegalArgumentException(name + " is required");
        return value;
    }
    private static long longValue(Map<String, Object> values, String name, long fallback) {
        Object value = values.get(name);
        if (value instanceof Number number) return number.longValue();
        if (value == null || value.toString().isBlank()) return fallback;
        return Long.parseLong(value.toString());
    }
}
