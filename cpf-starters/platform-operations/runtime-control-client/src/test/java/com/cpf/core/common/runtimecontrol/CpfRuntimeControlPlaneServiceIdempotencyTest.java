package com.cpf.core.common.runtimecontrol;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfRuntimeControlPlaneServiceIdempotencyTest {

    @Test
    void operationInsertRaceRevalidatesWinningFingerprintBeforeMutation() {
        CpfRuntimeControlPlaneRepository repository = mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        when(repository.findOperation("operation-1"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(Map.of(
                        "operation_id", "operation-1",
                        "command_type", "RUNTIME_CANCEL",
                        "request_hash", "other-request-hash",
                        "result_state", "PROCESSING",
                        "expires_at", Instant.now().plusSeconds(60))));
        when(repository.insertOperation(anyString(), anyString(), anyString(), any(Instant.class)))
                .thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.cancel(
                "change-1", "operation-1", "operator reason", "operator-1"));

        verify(repository, never()).consumeRateLimit(anyString(), anyInt());
        verify(repository, never()).cancel(anyString(), anyString(), anyString());
    }

    @Test
    void inProgressReplayDoesNotExecuteMutationOrConsumeRateLimit() {
        CpfRuntimeControlPlaneRepository repository = mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        String requestHash = CpfRuntimeCanonicalHash.sha256(
                Map.of("changeId", "change-1", "reason", "operator reason", "operatorId", "operator-1"));
        when(repository.findOperation("operation-1"))
                .thenReturn(Optional.of(Map.of(
                        "operation_id", "operation-1",
                        "command_type", "RUNTIME_CANCEL",
                        "request_hash", requestHash,
                        "result_state", "PROCESSING")));

        assertThrows(IllegalStateException.class, () -> service.cancel(
                "change-1", "operation-1", "operator reason", "operator-1"));

        verify(repository, never()).consumeRateLimit(anyString(), anyInt());
        verify(repository, never()).cancel(anyString(), anyString(), anyString());
    }

    @Test
    void differentOperatorCannotReplayAnotherOperatorsMutation() {
        CpfRuntimeControlPlaneRepository repository = mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        String originalHash = CpfRuntimeCanonicalHash.sha256(
                Map.of("changeId", "change-1", "reason", "operator reason", "operatorId", "operator-1"));
        when(repository.findOperation("operation-1"))
                .thenReturn(Optional.of(Map.of(
                        "operation_id", "operation-1",
                        "command_type", "RUNTIME_CANCEL",
                        "request_hash", originalHash,
                        "result_state", "SUCCESS",
                        "entity_id", "change-1")));

        assertThrows(IllegalStateException.class, () -> service.cancel(
                "change-1", "operation-1", "operator reason", "operator-2"));

        verify(repository, never()).cancel(anyString(), anyString(), anyString());
    }

    @Test
    void successfulReplayRejectsMismatchedStoredEntity() {
        CpfRuntimeControlPlaneRepository repository = mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        String hash = CpfRuntimeCanonicalHash.sha256(
                Map.of("changeId", "change-1", "reason", "operator reason", "operatorId", "operator-1"));
        when(repository.findOperation("operation-1"))
                .thenReturn(Optional.of(Map.of(
                        "operation_id", "operation-1",
                        "command_type", "RUNTIME_CANCEL",
                        "request_hash", hash,
                        "result_state", "SUCCESS",
                        "entity_id", "change-other")));

        assertThrows(IllegalStateException.class, () -> service.cancel(
                "change-1", "operation-1", "operator reason", "operator-1"));

        verify(repository, never()).cancel(anyString(), anyString(), anyString());
    }


    @Test
    void operationIdCannotCrossCommandTypes() {
        CpfRuntimeControlPlaneRepository repository = mock(CpfRuntimeControlPlaneRepository.class);
        CpfRuntimeControlPlaneService service = new CpfRuntimeControlPlaneService(repository);
        String hash = CpfRuntimeCanonicalHash.sha256(
                Map.of("changeId", "change-1", "reason", "operator reason", "operatorId", "operator-1"));
        when(repository.findOperation("operation-1"))
                .thenReturn(Optional.of(Map.of(
                        "operation_id", "operation-1",
                        "command_type", "RUNTIME_GROUP_DELETE",
                        "request_hash", hash,
                        "result_state", "SUCCESS",
                        "entity_id", "change-1")));

        assertThrows(IllegalStateException.class, () -> service.cancel(
                "change-1", "operation-1", "operator reason", "operator-1"));

        verify(repository, never()).cancel(anyString(), anyString(), anyString());
    }

}
