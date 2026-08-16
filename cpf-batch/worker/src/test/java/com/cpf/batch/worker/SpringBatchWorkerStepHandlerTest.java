package com.cpf.batch.worker;

import com.cpf.batch.api.BatchApprovedExecutorSnapshot;
import com.cpf.batch.api.BatchJobDefinition;
import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.batch.spi.FileProcessHandler;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpringBatchWorkerStepHandlerTest {

    private final ApprovedShellExecutor shell = mock(ApprovedShellExecutor.class);
    private final ApprovedFileExecutor files = mock(ApprovedFileExecutor.class);
    private final BatchFileProcessHandlerRegistry fileHandlers = mock(BatchFileProcessHandlerRegistry.class);
    private final BatchRuntimeExecutorRegistry external = mock(BatchRuntimeExecutorRegistry.class);
    private final SpringBatchWorkerStepHandler handler =
            new SpringBatchWorkerStepHandler(shell, files, fileHandlers, external);

    @Test
    void shellReceivesOnlyOriginalBusinessParameterNames() throws Exception {
        when(shell.execute(eq("SCRIPT:DAILY_CLOSE"), anyMap()))
                .thenReturn(new ApprovedShellExecutor.Result(true, 0, "completed"));

        handler.execute(command(
                BatchJobDefinition.ExecutorType.APPROVED_SHELL,
                "SCRIPT:DAILY_CLOSE",
                jobParameters(Map.of("arg.businessDate", "2026-08-02")),
                Map.of("fixedMode", "SAFE")));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(Map.class);
        verify(shell).execute(eq("SCRIPT:DAILY_CLOSE"), parameters.capture());
        assertEquals(Map.of("businessDate", "2026-08-02", "fixedMode", "SAFE"),
                parameters.getValue());
        assertFalse(parameters.getValue().containsKey("arg.businessDate"));
        assertFalse(parameters.getValue().containsKey("definitionChecksum"));
    }

    @Test
    void fileProcessUsesReservedApprovalScalarsAndUnwrappedBusinessParameters() throws Exception {
        Path claimed = Path.of("build", "handler-test", "claimed.csv").toAbsolutePath();
        FileProcessHandler processor = mock(FileProcessHandler.class);
        when(files.claimForProcess("IN", "daily.csv", "PROCESSING")).thenReturn(claimed);
        when(files.fingerprint(claimed)).thenReturn(new ApprovedFileExecutor.FileFingerprint(
                "daily.csv", 12L, "a".repeat(64), Instant.parse("2026-08-02T00:00:00Z")));
        when(files.transfer("PROCESSING", "daily.csv", "DONE", "daily.csv", false))
                .thenReturn(claimed);
        when(files.resolve("PROCESSING", "daily.csv")).thenReturn(claimed);
        when(fileHandlers.require("CSV_COUNT")).thenReturn(processor);
        when(processor.process(any())).thenReturn(
                FileProcessHandler.FileProcessResult.completed("processed", "b".repeat(64)));

        handler.execute(command(
                BatchJobDefinition.ExecutorType.FILE_PROCESS,
                "PROCESSOR:CSV_COUNT",
                jobParameters(Map.of(
                        "arg.sourceAlias", "IN",
                        "arg.sourcePath", "daily.csv",
                        "arg.processingAlias", "PROCESSING",
                        "arg.completedAlias", "DONE",
                        "arg.failedAlias", "FAILED",
                        "arg.transactionId", "tx-37")),
                Map.of()));

        ArgumentCaptor<FileProcessHandler.FileProcessCommand> processCommand =
                ArgumentCaptor.forClass(FileProcessHandler.FileProcessCommand.class);
        verify(processor).process(processCommand.capture());
        assertEquals(37L, processCommand.getValue().definitionVersion());
        assertEquals("c".repeat(64), processCommand.getValue().definitionChecksum());
        assertEquals("IN", processCommand.getValue().parameters().get("sourceAlias"));
        assertEquals("daily.csv", processCommand.getValue().parameters().get("sourcePath"));
        assertFalse(processCommand.getValue().parameters().containsKey("arg.sourceAlias"));
        assertFalse(processCommand.getValue().parameters().containsKey("definitionVersion"));
    }

    @Test
    void externalRebuildsImmutableApprovedSnapshotFromReservedScalars() throws Exception {
        when(external.execute(any(), anyMap(), anyLong(), eq("tx-37"), eq("external-step")))
                .thenReturn(BatchRuntimeExecutorRegistry.ExecutionResult.completed("accepted", 1));

        handler.execute(command(
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                "SERVICE:billing:charge",
                jobParameters(Map.of(
                        "executorType", "SERVICE_CALL",
                        "executorReference", "SERVICE:billing:charge",
                        "arg.transactionId", "tx-37",
                        "arg.requestPath", "/charges")),
                Map.of()));

        ArgumentCaptor<BatchApprovedExecutorSnapshot> snapshot =
                ArgumentCaptor.forClass(BatchApprovedExecutorSnapshot.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> parameters = ArgumentCaptor.forClass(Map.class);
        verify(external).execute(snapshot.capture(), parameters.capture(), eq(101L),
                eq("tx-37"), eq("external-step"));
        assertEquals("BAT.QA37", snapshot.getValue().jobId());
        assertEquals(37L, snapshot.getValue().definitionVersion());
        assertEquals("c".repeat(64), snapshot.getValue().definitionChecksum());
        assertEquals(BatchJobDefinition.ExecutorType.SERVICE_CALL, snapshot.getValue().executorType());
        assertEquals("SERVICE:billing:charge", snapshot.getValue().executorReference());
        assertEquals(120L, snapshot.getValue().timeoutSeconds());
        assertEquals(3, snapshot.getValue().maxAttempts());
        assertEquals(Map.of("transactionId", "tx-37", "requestPath", "/charges"),
                parameters.getValue());
    }

    @Test
    void rejectsBusinessArgumentThatCollidesWithCpfReservedParameter() throws Exception {
        BatchStepHandler.BatchStepCommand command = command(
                BatchJobDefinition.ExecutorType.APPROVED_SHELL,
                "SCRIPT:DAILY_CLOSE",
                jobParameters(Map.of("arg.jobId", "ATTACKER_JOB")),
                Map.of());

        assertThrows(SecurityException.class, () -> handler.execute(command));
        verify(shell, never()).execute(any(), anyMap());
    }

    @Test
    void rejectsApprovedSnapshotThatDoesNotMatchStepBinding() throws Exception {
        BatchStepHandler.BatchStepCommand command = command(
                BatchJobDefinition.ExecutorType.SERVICE_CALL,
                "SERVICE:billing:charge",
                jobParameters(Map.of(
                        "executorType", "SERVICE_CALL",
                        "executorReference", "SERVICE:billing:refund")),
                Map.of());

        assertThrows(SecurityException.class, () -> handler.execute(command));
        verify(external, never()).execute(any(), anyMap(), anyLong(), any(), any());
    }

    private static BatchStepHandler.BatchStepCommand command(
            BatchJobDefinition.ExecutorType executorType,
            String executorReference,
            Map<String, Object> jobParameters,
            Map<String, Object> stepParameters) {
        return new BatchStepHandler.BatchStepCommand(
                "cpf-execution-37",
                101L,
                202L,
                303L,
                new BatchStepDefinition(
                        "external-step", executorType, executorReference, stepParameters,
                        1, "", "", true),
                jobParameters,
                Map.of());
    }

    private static Map<String, Object> jobParameters(Map<String, Object> overrides) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put("cpfExecutionId", "cpf-execution-37");
        values.put("jobId", "BAT.QA37");
        values.put("definitionVersion", 37L);
        values.put("definitionChecksum", "c".repeat(64));
        values.put("executorType", "SERVICE_CALL");
        values.put("executorReference", "SERVICE:default:execute");
        values.put("timeoutSeconds", 120L);
        values.put("maxAttempts", 3L);
        values.putAll(overrides);
        return Map.copyOf(values);
    }
}
