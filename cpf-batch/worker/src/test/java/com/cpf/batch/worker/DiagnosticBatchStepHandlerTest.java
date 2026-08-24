package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchStepHandler;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DiagnosticBatchStepHandlerTest {
    @Test
    void executesInsideTrackedWorkerScopeAndReturnsDurableIdentityCheckpoint() throws Exception {
        SpringBatchWorkerRuntimeState runtime = mock(SpringBatchWorkerRuntimeState.class);
        when(runtime.workerId()).thenReturn("worker-diagnostic-1");
        WorkerExecutionTracker tracker = new WorkerExecutionTracker();
        DiagnosticBatchStepHandler handler = new DiagnosticBatchStepHandler(runtime, tracker);

        BatchStepHandler.BatchStepResult result = handler.execute(command("0"));

        assertEquals(BatchStepHandler.Status.COMPLETED, result.status());
        assertEquals("worker-diagnostic-1", result.checkpoint().get("diagnostic.workerId"));
        assertEquals(0, tracker.snapshot().inFlightInvocations());
        assertFalse(genericHandler().supports(
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                DiagnosticBatchStepHandler.REFERENCE));
    }

    @Test
    void rejectsOutOfRangeSleep() {
        DiagnosticBatchStepHandler handler = new DiagnosticBatchStepHandler(
                mock(SpringBatchWorkerRuntimeState.class), new WorkerExecutionTracker());
        assertThrows(IllegalArgumentException.class, () -> handler.execute(command("60001")));
    }

    private static BatchStepHandler.BatchStepCommand command(String sleepMs) {
        BatchStepDefinition step = new BatchStepDefinition(
                "diagnostic-remote-step",
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                DiagnosticBatchStepHandler.REFERENCE,
                Map.of("sleepMs", sleepMs),
                2,
                "",
                "",
                true);
        return new BatchStepHandler.BatchStepCommand(
                "BAT-diagnostic", 11L, 12L, 13L, step, Map.of(), Map.of());
    }

    private static SpringBatchWorkerStepHandler genericHandler() {
        return new SpringBatchWorkerStepHandler(
                mock(ApprovedShellExecutor.class),
                mock(ApprovedFileExecutor.class),
                mock(BatchFileProcessHandlerRegistry.class),
                mock(BatchRuntimeExecutorRegistry.class));
    }
}
