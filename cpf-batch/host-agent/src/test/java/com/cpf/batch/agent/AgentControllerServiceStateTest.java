package com.cpf.batch.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.batch.agent.internal.ArtifactInstaller;
import com.cpf.batch.agent.internal.LogArchiveService;
import com.cpf.batch.agent.internal.RuntimeControlProxy;
import com.cpf.batch.agent.internal.ServiceManager;
import com.cpf.batch.api.AgentCommandResult;
import com.cpf.batch.api.CommandState;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AgentControllerServiceStateTest {
    private static final String COMMAND_ID = "batcmd-" + "a".repeat(64);

    private final ApprovedCommandCatalog catalog = mock(ApprovedCommandCatalog.class);
    private final ArtifactInstaller installer = mock(ArtifactInstaller.class);
    private final ServiceManager manager = mock(ServiceManager.class);
    private final RuntimeControlProxy runtime = mock(RuntimeControlProxy.class);
    private final LogArchiveService logs = mock(LogArchiveService.class);
    private final AgentCommandLedger ledger = mock(AgentCommandLedger.class);
    private AgentController controller;

    @BeforeEach
    void setUp() {
        when(ledger.execute(anyString(), anyString(), anyString(), anyString(),
                any(AgentCommandLedger.CommandAction.class)))
                .thenAnswer(invocation -> {
                    String commandId = invocation.getArgument(0);
                    AgentCommandLedger.CommandAction action = invocation.getArgument(4);
                    return action.run(commandId, Instant.parse("2026-08-05T00:00:00Z"));
                });
        controller = new AgentController(
                catalog,
                installer,
                manager,
                runtime,
                logs,
                ledger,
                new ObjectMapper());
    }

    @Test
    void successfulStartCommandIsUnknownUntilRunningStateIsConfirmed() throws Exception {
        when(manager.execute("svc", ServiceManager.Action.START))
                .thenReturn(new ServiceManager.Result(true, 0, "accepted"));
        when(manager.state("svc")).thenReturn(ServiceManager.ServiceState.STOPPED);

        AgentCommandResult result = controller.start("svc", COMMAND_ID, COMMAND_ID);

        assertThat(result.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        assertThat(result.resultCode()).isEqualTo("SERVICE_POSTCONDITION_NOT_CONFIRMED");
    }

    @Test
    void successfulStopCommandIsUnknownWhileRuntimeStillRuns() throws Exception {
        when(manager.execute("svc", ServiceManager.Action.STOP))
                .thenReturn(new ServiceManager.Result(true, 0, "accepted"));
        when(manager.state("svc")).thenReturn(ServiceManager.ServiceState.RUNNING);

        AgentCommandResult result = controller.stop("svc", COMMAND_ID, COMMAND_ID);

        assertThat(result.state()).isEqualTo(CommandState.UNKNOWN_RESULT);
        assertThat(result.resultCode()).isEqualTo("SERVICE_POSTCONDITION_NOT_CONFIRMED");
    }

    @Test
    void stoppedStatusIsAConfirmedSuccessfulQuery() throws Exception {
        when(manager.state("svc")).thenReturn(ServiceManager.ServiceState.STOPPED);

        AgentCommandResult result = controller.status("svc", COMMAND_ID, COMMAND_ID);

        assertThat(result.state()).isEqualTo(CommandState.SUCCEEDED);
        assertThat(result.resultCode()).isEqualTo("SERVICE_STOPPED");
    }

    @Test
    void rollbackRemainsPartialWhenRestartPostconditionIsNotConfirmed() throws Exception {
        when(manager.execute("svc", ServiceManager.Action.STOP))
                .thenReturn(new ServiceManager.Result(true, 0, "stopped"));
        when(manager.stopped("svc")).thenReturn(true);
        when(installer.rollback("svc")).thenReturn("v0");
        when(manager.execute("svc", ServiceManager.Action.START))
                .thenReturn(new ServiceManager.Result(true, 0, "accepted"));
        when(manager.state("svc")).thenReturn(ServiceManager.ServiceState.STOPPED);

        AgentCommandResult result = controller.rollback("svc", COMMAND_ID, COMMAND_ID);

        assertThat(result.state()).isEqualTo(CommandState.PARTIALLY_ROLLED_BACK);
        assertThat(result.resultCode()).isEqualTo("ROLLBACK_START_NOT_CONFIRMED");
        assertThat(result.activeVersion()).isEqualTo("v0");
    }
}
