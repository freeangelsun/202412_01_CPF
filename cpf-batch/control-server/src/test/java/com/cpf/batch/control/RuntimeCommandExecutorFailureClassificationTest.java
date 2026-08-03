package com.cpf.batch.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.RuntimeCommand;
import com.cpf.batch.control.deploy.RuntimeLifecycleService;
import com.cpf.batch.control.internal.JdbcRuntimeCommandRepository;
import com.cpf.batch.control.internal.JdbcRuntimeRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RuntimeCommandExecutorFailureClassificationTest {
    private JdbcRuntimeCommandRepository commands;
    private JdbcRuntimeRegistry registry;
    private RuntimeLifecycleService lifecycle;
    private RuntimeCommandExecutor executor;

    @BeforeEach
    void setUp() {
        commands = mock(JdbcRuntimeCommandRepository.class);
        registry = mock(JdbcRuntimeRegistry.class);
        lifecycle = mock(RuntimeLifecycleService.class);
        executor = new RuntimeCommandExecutor(commands, registry, lifecycle);
    }


    @Test
    void duplicateTargetsAreRejectedBeforeDispatch() {
        RuntimeCommand command = command(List.of("runtime-1", "runtime-1"));
        persisted(command, CommandState.APPROVED);

        executor.execute(command);

        verify(lifecycle, never()).operate(
                eq("runtime-1"), eq("RESTART"), eq("requester"), eq("approver"),
                eq("APR-1"), eq("approved maintenance"));
        verify(commands).transition(
                eq("CMD-1"), eq(CommandState.FAILED), eq("VALIDATION"),
                eq("Duplicate target IDs are not allowed"));
    }

    @Test
    void validationPersistenceFailureUsesStableUnknownClassification() {
        RuntimeCommand command = command(List.of("runtime-1", "runtime-1"));
        persisted(command, CommandState.APPROVED);
        doThrow(new IllegalStateException("command store unavailable"))
                .when(commands).transition(
                        eq("CMD-1"), eq(CommandState.FAILED), eq("VALIDATION"),
                        eq("Duplicate target IDs are not allowed"));

        RuntimeCommandExecutionException failure = assertThrows(
                RuntimeCommandExecutionException.class,
                () -> executor.execute(command));

        assertEquals("BATCH_RUNTIME_COMMAND_VALIDATION_FINALIZE_UNKNOWN", failure.code());
        assertEquals(CommandState.UNKNOWN_RESULT, failure.state());
        verify(lifecycle, never()).operate(
                eq("runtime-1"), eq("RESTART"), eq("requester"), eq("approver"),
                eq("APR-1"), eq("approved maintenance"));
    }

    @Test
    void snapshotFailureBeforeDispatchIsDeterministicFailed() {
        RuntimeCommand command = command(List.of("runtime-1"));
        persisted(command, CommandState.APPROVED);
        when(commands.beginExecution(command.commandId())).thenReturn(true);
        when(registry.snapshot("runtime-1"))
                .thenThrow(new IllegalArgumentException("Runtime instance not found"));

        executor.execute(command);

        verify(lifecycle, never()).operate(
                eq("runtime-1"), eq("RESTART"), eq("requester"), eq("approver"),
                eq("APR-1"), eq("approved maintenance"));
        verify(commands).recordAttempt(
                eq("CMD-1"), eq(1), eq("runtime-1"), eq("CONTROL_SNAPSHOT"),
                eq(CommandState.FAILED), argThat(message -> message.contains("Runtime instance not found")));
        verify(commands).transition(
                eq("CMD-1"), eq(CommandState.FAILED), eq("CONTROL_SNAPSHOT"),
                argThat(summary -> summary.contains("runtime-1=FAILED")));
    }

    @Test
    void responseLossAfterDispatchIsUnknownAndSecretIsMasked() {
        RuntimeCommand command = command(List.of("runtime-1"));
        persisted(command, CommandState.APPROVED);
        when(commands.beginExecution(command.commandId())).thenReturn(true);
        when(registry.snapshot("runtime-1")).thenReturn(Map.of("actual_state", "RUNNING"));
        when(lifecycle.operate(
                "runtime-1", "RESTART", "requester", "approver", "APR-1",
                "approved maintenance"))
                .thenThrow(new IllegalStateException("token=raw-secret response lost"));

        executor.execute(command);

        verify(commands).recordAttempt(
                eq("CMD-1"), eq(1), eq("runtime-1"), eq("OWNER_API_DISPATCH"),
                eq(CommandState.UNKNOWN_RESULT),
                argThat(message -> message.contains("token=<masked>")
                        && !message.contains("raw-secret")));
        verify(commands).transition(
                eq("CMD-1"), eq(CommandState.UNKNOWN_RESULT), eq("OWNER_API_DISPATCH"),
                argThat(summary -> summary.contains("runtime-1=UNKNOWN_RESULT")));
    }

    @Test
    void attemptEvidenceFailureAfterAgentSuccessIsUnknown() {
        RuntimeCommand command = command(List.of("runtime-1"));
        persisted(command, CommandState.APPROVED);
        when(commands.beginExecution(command.commandId())).thenReturn(true);
        when(registry.snapshot("runtime-1")).thenReturn(Map.of("actual_state", "RUNNING"));
        when(lifecycle.operate(
                "runtime-1", "RESTART", "requester", "approver", "APR-1",
                "approved maintenance"))
                .thenReturn(result(CommandState.SUCCEEDED, "restarted"));
        doThrow(new IllegalStateException("attempt store unavailable"))
                .when(commands)
                .recordAttempt(
                        eq("CMD-1"), eq(1), eq("runtime-1"), eq("AGENT_RESTART"),
                        eq(CommandState.SUCCEEDED), eq("restarted"));

        executor.execute(command);

        verify(commands).transition(
                eq("CMD-1"), eq(CommandState.UNKNOWN_RESULT),
                eq("ATTEMPT_EVIDENCE_PERSISTENCE"),
                argThat(summary -> summary.contains("runtime-1=UNKNOWN_RESULT")));
    }

    @Test
    void finalTransitionFailureDoesNotReportSuccess() {
        RuntimeCommand command = command(List.of("runtime-1"));
        persisted(command, CommandState.APPROVED);
        when(commands.beginExecution(command.commandId())).thenReturn(true);
        when(registry.snapshot("runtime-1")).thenReturn(Map.of("actual_state", "RUNNING"));
        when(lifecycle.operate(
                "runtime-1", "RESTART", "requester", "approver", "APR-1",
                "approved maintenance"))
                .thenReturn(result(CommandState.SUCCEEDED, "restarted"));
        doThrow(new IllegalStateException("command store unavailable"))
                .when(commands)
                .transition(
                        eq("CMD-1"), eq(CommandState.SUCCEEDED), eq(null),
                        argThat(summary -> summary.contains("runtime-1=SUCCEEDED")));

        RuntimeCommandExecutionException failure = assertThrows(
                RuntimeCommandExecutionException.class,
                () -> executor.execute(command));

        assertEquals("BATCH_RUNTIME_COMMAND_FINALIZE_UNKNOWN", failure.code());
        assertEquals(CommandState.UNKNOWN_RESULT, failure.state());
    }

    private void persisted(RuntimeCommand command, CommandState resultState) {
        when(commands.create(command)).thenReturn(Map.of("command_id", command.commandId()));
        when(commands.find(command.idempotencyKey())).thenReturn(Optional.of(Map.of(
                "command_id", command.commandId(), "execution_state", resultState.name())));
    }

    private static RuntimeCommand command(List<String> targets) {
        Instant now = Instant.now();
        return new RuntimeCommand(
                "CMD-1", "IDEM-1", "RESTART", "INSTANCE", targets,
                "snapshot", "snapshot-hash", 7L, "requester", "approved maintenance",
                now, "POLICY-1", "APR-1", "approver", now.plusSeconds(300),
                CommandState.APPROVED, 0, Map.of(), null, null, "before", null,
                "OBAT-AA-00000000000000000000000000", null);
    }

    private static AgentCommandResult result(CommandState state, String message) {
        Instant now = Instant.now();
        return new AgentCommandResult(
                "agent-command", "service-1", "restart", state,
                "OK", message, "1.0.0", now, now.plusSeconds(1));
    }
}
