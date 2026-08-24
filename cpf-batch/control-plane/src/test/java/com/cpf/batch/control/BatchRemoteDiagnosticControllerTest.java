package com.cpf.batch.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.BatchApprovedLaunchRequest;
import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.api.BatchExecutionLink;
import com.cpf.batch.api.BatchExecutionTopology;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BatchRemoteDiagnosticControllerTest {
    private static final String TOKEN = "diagnostic-token-value-1234567890abcdef";

    @Test
    void loopbackTokenLaunchesValidatedRemotePartitionPlan() {
        BatchExecutionControlPort executions = mock(BatchExecutionControlPort.class);
        BatchExecutionLink link = new BatchExecutionLink(
                "BAT-test", BatchRemoteDiagnosticController.JOB_ID, 1L,
                11L, 12L, null, "COMPLETED", 41L, Instant.EPOCH);
        when(executions.start(any())).thenReturn(link);
        BatchRemoteDiagnosticController controller = new BatchRemoteDiagnosticController(executions, TOKEN);

        assertEquals(link, controller.start(
                TOKEN,
                new BatchRemoteDiagnosticController.Command("diag-run-0001", 41L, 8, 25L),
                request("127.0.0.1")).getBody());

        ArgumentCaptor<BatchApprovedLaunchRequest> approved =
                ArgumentCaptor.forClass(BatchApprovedLaunchRequest.class);
        verify(executions).start(approved.capture());
        assertEquals(BatchExecutionTopology.REMOTE_PARTITION, approved.getValue().plan().topology());
        assertEquals(8, approved.getValue().plan().steps().getFirst().partitionCount());
        assertEquals(BatchRemoteDiagnosticController.EXECUTOR_REFERENCE,
                approved.getValue().plan().steps().getFirst().executorReference());
        approved.getValue().plan().verifyIntegrity();
    }

    @Test
    void rejectsRemoteCallerWrongTokenAndUnsafeBoundsBeforeLaunch() {
        BatchExecutionControlPort executions = mock(BatchExecutionControlPort.class);
        BatchRemoteDiagnosticController controller = new BatchRemoteDiagnosticController(executions, TOKEN);
        BatchRemoteDiagnosticController.Command valid =
                new BatchRemoteDiagnosticController.Command("diag-run-0002", 42L, 2, 0L);

        assertThrows(SecurityException.class, () -> controller.start(TOKEN, valid, request("192.0.2.10")));
        assertThrows(SecurityException.class, () ->
                controller.start("wrong-token-value-1234567890abcdef", valid, request("::1")));
        assertThrows(IllegalArgumentException.class, () -> controller.start(
                TOKEN,
                new BatchRemoteDiagnosticController.Command("diag-run-0003", 43L, 1, 0L),
                request("127.0.0.1")));
    }

    @Test
    void refusesWeakConfiguredToken() {
        assertThrows(IllegalStateException.class, () ->
                new BatchRemoteDiagnosticController(mock(BatchExecutionControlPort.class), "short"));
    }

    private static HttpServletRequest request(String remoteAddress) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn(remoteAddress);
        return request;
    }
}
