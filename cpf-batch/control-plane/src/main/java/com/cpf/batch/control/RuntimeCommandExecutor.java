package com.cpf.batch.control;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DesiredState;
import com.cpf.batch.api.RuntimeCommand;
import com.cpf.batch.control.deploy.RuntimeLifecycleService;
import com.cpf.batch.control.internal.JdbcRuntimeCommandRepository;
import com.cpf.batch.control.internal.JdbcRuntimeRegistry;
import com.cpf.batch.control.internal.RuntimeCommandIdempotencyConflictException;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;

/**
 * 승인된 Runtime command를 대상별로 실행하고 부분 실패/UNKNOWN을 보존합니다.
 *
 * <p>외부 Agent 호출 전에 발생한 Snapshot/CAS 오류는 결정적 FAILED로 기록합니다.
 * Agent 호출이 시작된 뒤 응답이 없거나 성공 결과의 Evidence 저장이 실패한 경우에만
 * UNKNOWN_RESULT로 분류해 blind retry를 방지합니다.</p>
 */
@Service
public class RuntimeCommandExecutor {
    private static final Set<String> RISKY = Set.of(
            "START", "STOP", "RESTART", "DRAIN", "RESUME", "ROLLBACK");

    private final JdbcRuntimeCommandRepository commands;
    private final JdbcRuntimeRegistry registry;
    private final RuntimeLifecycleService lifecycle;

    public RuntimeCommandExecutor(
            JdbcRuntimeCommandRepository commands,
            JdbcRuntimeRegistry registry,
            RuntimeLifecycleService lifecycle) {
        this.commands = Objects.requireNonNull(commands, "commands");
        this.registry = Objects.requireNonNull(registry, "registry");
        this.lifecycle = Objects.requireNonNull(lifecycle, "lifecycle");
    }

    Map<String, Object> execute(RuntimeCommand command) {
        RuntimeCommand normalized = RuntimeCommandIdentity.normalize(command);
        Map<String, Object> persisted;
        try {
            persisted = commands.create(normalized);
        } catch (RuntimeCommandIdempotencyConflictException conflict) {
            throw new RuntimeCommandExecutionException(
                    "BATCH_RUNTIME_COMMAND_IDEMPOTENCY_CONFLICT",
                    CommandState.FAILED,
                    "Runtime commandId is already bound to another idempotency key",
                    conflict);
        } catch (RuntimeException failure) {
            throw new RuntimeCommandExecutionException(
                    "BATCH_RUNTIME_COMMAND_PERSISTENCE_UNKNOWN",
                    CommandState.UNKNOWN_RESULT,
                    "Runtime command persistence is unavailable. Retry only with the same idempotency key",
                    failure);
        }
        RuntimeCommandIdentity.assertMatches(normalized, persisted);
        command = normalized;

        String type = normalizeType(command.commandType());
        String validationError = validate(command, type);
        if (validationError != null) {
            return fail(command, validationError.substring(0, validationError.indexOf(':')),
                    validationError.substring(validationError.indexOf(':') + 1));
        }
        if (!commands.beginExecution(command.commandId())) {
            return findRequired(command.idempotencyKey(), "BATCH_RUNTIME_COMMAND_CONCURRENT_EXECUTION");
        }

        Aggregate aggregate = new Aggregate();
        int attempt = 0;
        for (String target : command.targetIds()) {
            attempt++;
            aggregate.accept(executeTarget(command, type, target, attempt));
        }

        CommandState finalState = aggregate.finalState();
        String failureStage = aggregate.failureStage(finalState);
        String summary = aggregate.summary();
        try {
            commands.transition(command.commandId(), finalState, failureStage, summary);
        } catch (RuntimeException failure) {
            throw new RuntimeCommandExecutionException(
                    "BATCH_RUNTIME_COMMAND_FINALIZE_UNKNOWN",
                    CommandState.UNKNOWN_RESULT,
                    "Target operations completed but command finalization is unknown. Reconcile commandId="
                            + command.commandId(),
                    failure);
        }
        return findRequired(command.idempotencyKey(), "BATCH_RUNTIME_COMMAND_RESULT_MISSING");
    }

    private TargetOutcome executeTarget(
            RuntimeCommand command,
            String type,
            String target,
            int attempt) {
        try {
            registry.snapshot(target);
        } catch (RuntimeException failure) {
            return deterministicFailure(command, target, attempt, "CONTROL_SNAPSHOT", failure);
        }

        DesiredState desired = desired(type);
        if (desired != null) {
            try {
                registry.updateDesiredState(target, desired, command.expectedVersion());
            } catch (RuntimeException failure) {
                return deterministicFailure(command, target, attempt, "DESIRED_STATE_UPDATE", failure);
            }
        }

        AgentCommandResult result;
        try {
            result = lifecycle.operate(
                    target,
                    type,
                    command.requestedBy(),
                    command.approvedBy(),
                    command.approvalRequestId(),
                    command.reason());
        } catch (RuntimeException failure) {
            String message = safe(failure);
            boolean dispatchEvidencePersisted = recordAttemptBestEffort(
                    command.commandId(), attempt, target,
                    "OWNER_API_DISPATCH", CommandState.UNKNOWN_RESULT, message);
            if (!dispatchEvidencePersisted) {
                return new TargetOutcome(target, CommandState.UNKNOWN_RESULT,
                        "ATTEMPT_EVIDENCE_PERSISTENCE",
                        "Agent dispatch outcome evidence was not persisted", true);
            }
            return new TargetOutcome(target, CommandState.UNKNOWN_RESULT,
                    "OWNER_API_DISPATCH", message, true);
        }

        CommandState resultState = result == null || result.state() == null
                ? CommandState.UNKNOWN_RESULT
                : result.state();
        String stage = "AGENT_" + type;
        String message = result == null ? "Agent returned null result" : safe(result.message());
        if (!terminalAgentState(resultState)) {
            CommandState nonTerminal = resultState;
            resultState = CommandState.UNKNOWN_RESULT;
            stage = stage + "_NON_TERMINAL_RESULT";
            message = "Agent returned non-terminal state " + nonTerminal
                    + "; explicit reconcile is required";
        }
        boolean evidencePersisted = recordAttemptBestEffort(
                command.commandId(), attempt, target, stage, resultState, message);
        if (!evidencePersisted) {
            return new TargetOutcome(target, CommandState.UNKNOWN_RESULT,
                    "ATTEMPT_EVIDENCE_PERSISTENCE", "Agent outcome evidence was not persisted", true);
        }

        if (resultState == CommandState.SUCCEEDED && "ROLLBACK".equals(type)) {
            try {
                registry.updateDesiredState(target, DesiredState.RUNNING, 0L);
            } catch (RuntimeException failure) {
                String detail = safe(failure);
                boolean postRollbackEvidencePersisted = recordAttemptBestEffort(
                        command.commandId(), attempt, target,
                        "POST_ROLLBACK_STATE", CommandState.UNKNOWN_RESULT, detail);
                if (!postRollbackEvidencePersisted) {
                    return new TargetOutcome(target, CommandState.UNKNOWN_RESULT,
                            "ATTEMPT_EVIDENCE_PERSISTENCE",
                            "Post-rollback state evidence was not persisted", true);
                }
                return new TargetOutcome(target, CommandState.UNKNOWN_RESULT,
                        "POST_ROLLBACK_STATE", detail, true);
            }
        }
        return new TargetOutcome(target, resultState, stage, message,
                resultState == CommandState.UNKNOWN_RESULT);
    }

    private TargetOutcome deterministicFailure(
            RuntimeCommand command,
            String target,
            int attempt,
            String stage,
            RuntimeException failure) {
        String message = safe(failure);
        boolean persisted = recordAttemptBestEffort(
                command.commandId(), attempt, target, stage, CommandState.FAILED, message);
        return new TargetOutcome(target,
                persisted ? CommandState.FAILED : CommandState.UNKNOWN_RESULT,
                persisted ? stage : "ATTEMPT_EVIDENCE_PERSISTENCE",
                persisted ? message : "Deterministic failure evidence was not persisted",
                !persisted);
    }

    private boolean recordAttemptBestEffort(
            String commandId,
            int attempt,
            String target,
            String stage,
            CommandState state,
            String message) {
        try {
            commands.recordAttempt(commandId, attempt, target, stage, state, message);
            return true;
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private Map<String, Object> fail(RuntimeCommand command, String stage, String message) {
        try {
            commands.transition(command.commandId(), CommandState.FAILED, stage, message);
        } catch (RuntimeException failure) {
            throw new RuntimeCommandExecutionException(
                    "BATCH_RUNTIME_COMMAND_VALIDATION_FINALIZE_UNKNOWN",
                    CommandState.UNKNOWN_RESULT,
                    "Validation failed but command state persistence is unknown. Reconcile commandId="
                            + command.commandId(),
                    failure);
        }
        return findRequired(command.idempotencyKey(), "BATCH_RUNTIME_COMMAND_VALIDATION_RESULT_MISSING");
    }

    private Map<String, Object> findRequired(String idempotencyKey, String errorCode) {
        return commands.find(idempotencyKey)
                .orElseThrow(() -> new RuntimeCommandExecutionException(
                        errorCode,
                        CommandState.UNKNOWN_RESULT,
                        "Persisted command result is unavailable for idempotencyKey=" + idempotencyKey));
    }

    private static String validate(RuntimeCommand command, String type) {
        if (!RISKY.contains(type)) return "VALIDATION:Unsupported command";
        if (command.targetIds() == null || command.targetIds().isEmpty()
                || command.targetIds().stream().anyMatch(id -> id == null || id.isBlank())) {
            return "VALIDATION:At least one non-blank target is required";
        }
        long distinctTargets = command.targetIds().stream().map(String::trim).distinct().count();
        if (distinctTargets != command.targetIds().size()) {
            return "VALIDATION:Duplicate target IDs are not allowed";
        }
        if (command.approvalRequestId() == null || command.approvalRequestId().isBlank()
                || command.approvalPolicyVersion() == null || command.approvalPolicyVersion().isBlank()) {
            return "APPROVAL:Approved command reference/policy is required";
        }
        if (command.approvedBy() == null || command.approvedBy().isBlank()
                || command.approvedBy().equals(command.requestedBy())) {
            return "APPROVAL:Requester/approver separation is required";
        }
        if (command.expiresAt() == null) {
            return "EXPIRY:Approved command expiry is required";
        }
        if (!command.expiresAt().isAfter(java.time.Instant.now())) {
            return "EXPIRY:Approved command expired";
        }
        return null;
    }

    private static boolean terminalAgentState(CommandState state) {
        return state == CommandState.SUCCEEDED
                || state == CommandState.FAILED
                || state == CommandState.UNKNOWN_RESULT
                || state == CommandState.ROLLED_BACK
                || state == CommandState.PARTIALLY_ROLLED_BACK;
    }

    private static String normalizeType(String type) {
        return type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
    }

    private static DesiredState desired(String type) {
        return switch (type) {
            case "START", "RESTART", "RESUME" -> DesiredState.RUNNING;
            case "STOP" -> DesiredState.STOPPED;
            case "DRAIN" -> DesiredState.DRAINING;
            case "ROLLBACK" -> DesiredState.ROLLING_BACK;
            default -> null;
        };
    }

    private static String safe(Throwable failure) {
        return safe(failure == null
                ? "Unknown failure"
                : failure.getClass().getSimpleName() + ": "
                        + Objects.toString(failure.getMessage(), ""));
    }

    private static String safe(String value) {
        String sanitized = SensitiveTextSanitizer.sanitize(Objects.toString(value, ""));
        sanitized = sanitized.replaceAll("[\\r\\n\\t]+", " ").trim();
        return sanitized.length() <= 2_000 ? sanitized : sanitized.substring(0, 2_000);
    }

    private record TargetOutcome(
            String target,
            CommandState state,
            String stage,
            String message,
            boolean unknown) {
    }

    private static final class Aggregate {
        private final StringBuilder summary = new StringBuilder();
        private int total;
        private int succeeded;
        private int rolledBack;
        private boolean failed;
        private boolean unknown;
        private boolean partiallyRolledBack;
        private String firstFailedStage;
        private String firstUnknownStage;
        private String firstRollbackStage;

        void accept(TargetOutcome outcome) {
            total++;
            summary.append(outcome.target()).append('=').append(outcome.state()).append(';');
            if (outcome.unknown() || outcome.state() == CommandState.UNKNOWN_RESULT) {
                unknown = true;
                if (firstUnknownStage == null) firstUnknownStage = outcome.stage();
            } else if (outcome.state() == CommandState.PARTIALLY_ROLLED_BACK) {
                partiallyRolledBack = true;
                if (firstRollbackStage == null) firstRollbackStage = outcome.stage();
            } else if (outcome.state() == CommandState.ROLLED_BACK) {
                rolledBack++;
                if (firstRollbackStage == null) firstRollbackStage = outcome.stage();
            } else if (outcome.state() == CommandState.FAILED) {
                failed = true;
                if (firstFailedStage == null) firstFailedStage = outcome.stage();
            } else if (outcome.state() == CommandState.SUCCEEDED) {
                succeeded++;
            }
        }

        CommandState finalState() {
            if (unknown) return CommandState.UNKNOWN_RESULT;
            if (partiallyRolledBack) return CommandState.PARTIALLY_ROLLED_BACK;
            if (rolledBack > 0) {
                return rolledBack == total
                        ? CommandState.ROLLED_BACK
                        : CommandState.PARTIALLY_ROLLED_BACK;
            }
            if (failed) return CommandState.FAILED;
            return succeeded == total ? CommandState.SUCCEEDED : CommandState.UNKNOWN_RESULT;
        }

        String failureStage(CommandState state) {
            return state == CommandState.UNKNOWN_RESULT ? firstUnknownStage
                    : state == CommandState.FAILED ? firstFailedStage
                    : state == CommandState.PARTIALLY_ROLLED_BACK ? firstRollbackStage
                    : null;
        }

        String summary() {
            return summary.toString();
        }
    }
}
