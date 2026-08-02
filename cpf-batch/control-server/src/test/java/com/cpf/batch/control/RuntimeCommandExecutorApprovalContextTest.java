package com.cpf.batch.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DesiredState;
import com.cpf.batch.api.RuntimeCommand;
import com.cpf.batch.control.deploy.RuntimeLifecycleService;
import com.cpf.batch.control.internal.JdbcRuntimeCommandRepository;
import com.cpf.batch.control.internal.JdbcRuntimeRegistry;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class RuntimeCommandExecutorApprovalContextTest {
    @Test
    void propagatesVerifiedApprovalContextToTheAgentLifecycle() {
        JdbcRuntimeCommandRepository commands = mock(JdbcRuntimeCommandRepository.class);
        JdbcRuntimeRegistry registry = mock(JdbcRuntimeRegistry.class);
        RuntimeLifecycleService lifecycle = mock(RuntimeLifecycleService.class);
        RuntimeCommandExecutor executor = new RuntimeCommandExecutor(commands, registry, lifecycle);
        Instant now = Instant.now();
        RuntimeCommand command = new RuntimeCommand(
                "CMD-1", "IDEM-1", "RESTART", "INSTANCE", List.of("runtime-1"),
                "snapshot", "snapshot-hash", 7L, "requester", "approved maintenance",
                now, "POLICY-1", "APR-1", "approver", now.plusSeconds(300),
                CommandState.APPROVED, 0, Map.of(), null, null, "before", null,
                "OBAT-AA-00000000000000000000000000", null);
        Map<String, Object> persisted = Map.of("command_id", "CMD-1");
        Map<String, Object> completed = Map.of(
                "command_id", "CMD-1", "execution_state", "SUCCEEDED");
        AgentCommandResult result = new AgentCommandResult(
                "agent-command", "service-1", "restart", CommandState.SUCCEEDED,
                "OK", "restarted", "1.0.0", now, now.plusSeconds(1));

        when(commands.create(command)).thenReturn(persisted);
        when(commands.beginExecution("CMD-1")).thenReturn(true);
        when(commands.find("IDEM-1")).thenReturn(Optional.of(completed));
        when(registry.snapshot("runtime-1")).thenReturn(Map.of("actual_state", "RUNNING"));
        when(lifecycle.operate(
                "runtime-1", "RESTART", "requester", "approver", "APR-1",
                "approved maintenance"))
                .thenReturn(result);

        executor.execute(command);

        verify(registry).updateDesiredState("runtime-1", DesiredState.RUNNING, 7L);
        verify(lifecycle).operate(
                "runtime-1", "RESTART", "requester", "approver", "APR-1",
                "approved maintenance");
        verify(commands).recordAttempt(
                "CMD-1", 1, "runtime-1", "AGENT_RESTART", CommandState.SUCCEEDED,
                "restarted");
    }
}
