package com.cpf.admin.approval.service;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdmApprovalServiceExecutionTest {
    private static final String TRANSACTION_ID =
            "20260729170000000" + "ADM" + "admAP01" + "0000001";
    private static final String PAYLOAD = "{\"instanceId\":\"runtime-01\",\"token\":\"must-not-log\"}";

    @Test
    void executeReservesOncePropagatesImmutableSnapshotAndFinalizesAtomically() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveExecution(eq(42L), eq(3L), anyString(), eq("approver-b")))
                .thenReturn(true);
        when(repository.findReservedExecutionCommand(eq(42L), anyString())).thenReturn(Optional.of(reserved(request)));
        when(ownerPort.execute(any(AdmApprovedOperationCommand.class)))
                .thenReturn(new AdmApprovedOperationResult(
                        AdmApprovalExecutionStatus.SUCCEEDED,
                        "BAT-SUCCEEDED",
                        "done"));
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        service.execute(42L, "approved maintenance", "approver-b");

        ArgumentCaptor<AdmApprovedOperationCommand> command =
                ArgumentCaptor.forClass(AdmApprovedOperationCommand.class);
        verify(ownerPort).execute(command.capture());
        assertThat(command.getValue().requestedBy()).isEqualTo("requester-a");
        assertThat(command.getValue().approvedBy()).isEqualTo("approver-b");
        assertThat(command.getValue().reason()).isEqualTo("approved maintenance");
        assertThat(command.getValue().transactionId()).isEqualTo(TRANSACTION_ID);
        assertThat(command.getValue().payloadSnapshot()).isEqualTo(PAYLOAD);
        assertThat(command.getValue().leaseOwner()).isEqualTo("approver-b");
        assertThat(command.getValue().fenceToken()).isEqualTo(7L);
        verify(repository).reserveExecution(eq(42L), eq(3L), eq(command.getValue().commandRequestId()), eq("approver-b"));
        verify(repository).finishExecutionAndRequest(
                eq(42L), eq(4L), eq(command.getValue().commandRequestId()), eq("approver-b"), eq(7L), eq("SUCCEEDED"), eq("COMPLETED"),
                eq("BAT-SUCCEEDED"), eq("done"), eq(false), eq("approver-b"),
                eq("approved maintenance"), contains("\"executionStatus\":\"SUCCEEDED\""), eq(TRANSACTION_ID));
        verify(repository, never()).startExecution(anyLong(), anyString(), anyString());
        verify(repository, never()).updateRequest(anyLong(), anyLong(), anyString(), anyInt(), anyString());
    }

    @Test
    void executeReturnsExistingExecutionWhenAnotherInstanceWinsReservation() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        Map<String, Object> running = Map.of("executionStatus", "RUNNING");
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L))
                .thenReturn(Optional.empty(), Optional.of(running), Optional.of(running));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveExecution(eq(42L), eq(3L), anyString(), eq("approver-b")))
                .thenReturn(false);
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        Map<String, Object> detail = service.execute(42L, "approved maintenance", "approver-b");

        assertThat(detail.get("execution")).isEqualTo(running);
        verify(ownerPort, never()).execute(any());
        verify(repository, never()).finishExecutionAndRequest(
                anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any(), anyBoolean(),
                anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void executeConvertsOwnerExceptionToUnknownAndRequiresRecovery() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveExecution(eq(42L), eq(3L), anyString(), eq("approver-b")))
                .thenReturn(true);
        when(repository.findReservedExecutionCommand(eq(42L), anyString())).thenReturn(Optional.of(reserved(request)));
        when(ownerPort.execute(any())).thenThrow(new IllegalStateException("network response lost"));
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        service.execute(42L, "approved maintenance", "approver-b");

        verify(repository).finishExecutionAndRequest(
                eq(42L), eq(4L), anyString(), eq("approver-b"), eq(7L), eq("UNKNOWN"), eq("UNKNOWN"),
                eq("ADM-OWNER-EXCEPTION"), contains("확정할 수 없습니다"), eq(true), eq("approver-b"),
                eq("approved maintenance"), contains("\"executionStatus\":\"UNKNOWN\""), eq(TRANSACTION_ID));
    }

    @Test
    void executeMarksUnknownWhenOwnerResultCannotBePersistedAtomically() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveExecution(eq(42L), eq(3L), anyString(), eq("approver-b")))
                .thenReturn(true);
        when(repository.findReservedExecutionCommand(eq(42L), anyString())).thenReturn(Optional.of(reserved(request)));
        when(ownerPort.execute(any())).thenReturn(new AdmApprovedOperationResult(
                AdmApprovalExecutionStatus.SUCCEEDED, "BAT-SUCCEEDED", "done"));
        doThrow(new IllegalStateException("request finalization failed"))
                .when(repository).finishExecutionAndRequest(
                        anyLong(), anyLong(), anyString(), anyString(), anyLong(), anyString(), anyString(), any(), any(), anyBoolean(),
                        anyString(), anyString(), anyString(), anyString());
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        service.execute(42L, "approved maintenance", "approver-b");

        verify(repository).markExecutionUnknown(
                eq(42L), anyString(), eq("approver-b"), eq(7L), eq("ADM-FINALIZATION-UNKNOWN"),
                eq("Owner 호출 후 결과 저장을 확정할 수 없습니다."), eq("approver-b"));
        verify(repository).history(
                eq(42L), eq("RESULT_UNKNOWN"), eq("approver-b"), eq("EXECUTING"), eq("UNKNOWN"),
                eq("approved maintenance"), contains("\"failure\":\"FINALIZATION\""), eq(TRANSACTION_ID));
    }

    @Test
    void reconcileUnknownQueriesOwnerWithoutRepeatingMutationAndFinalizesRecovered() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        request.put("approvalStatus", "UNKNOWN");
        request.put("versionNo", 5L);
        request.put("payloadHash", new AdmApprovalSnapshotIntegrity(new ObjectMapper()).hash(request));
        Map<String, Object> execution = Map.of(
                "commandRequestId", "ADM-APP-42-original",
                "executionStatus", "UNKNOWN",
                "recoveryRequiredYn", "Y");
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.of(execution));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveReconcile(42L, 5L, "approver-b")).thenReturn(true);
        when(repository.findReservedExecutionCommand(42L, "ADM-APP-42-original"))
                .thenReturn(Optional.of(reserved(request)));
        when(ownerPort.reconcile(any())).thenReturn(new AdmApprovedOperationResult(
                AdmApprovalExecutionStatus.RECOVERED, "BAT-RECONCILED", "side effect applied"));
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        service.reconcile(42L, "resolve unknown", "approver-b");

        verify(ownerPort).reconcile(any(AdmApprovedOperationCommand.class));
        verify(ownerPort, never()).execute(any());
        verify(repository).finishExecutionAndRequest(
                eq(42L), eq(6L), eq("ADM-APP-42-original"), eq("approver-b"), eq(7L), eq("RECOVERED"), eq("COMPLETED"),
                eq("BAT-RECONCILED"), eq("side effect applied"), eq(false), eq("approver-b"),
                eq("resolve unknown"), contains("\"reconciliation\":true"), eq(TRANSACTION_ID));
    }

    @Test
    void reconcileUnknownKeepsUnknownWhenOwnerCannotProveOutcome() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        Map<String, Object> request = approvedRequest(Instant.now().plusSeconds(900));
        request.put("approvalStatus", "UNKNOWN");
        request.put("versionNo", 5L);
        request.put("payloadHash", new AdmApprovalSnapshotIntegrity(new ObjectMapper()).hash(request));
        Map<String, Object> execution = Map.of("commandRequestId", "CMD-UNKNOWN", "executionStatus", "UNKNOWN");
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.of(execution));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.reserveReconcile(42L, 5L, "approver-b")).thenReturn(true);
        when(repository.findReservedExecutionCommand(42L, "CMD-UNKNOWN")).thenReturn(Optional.of(reserved(request)));
        when(ownerPort.reconcile(any())).thenReturn(new AdmApprovedOperationResult(
                AdmApprovalExecutionStatus.UNKNOWN, "BAT-STILL-UNKNOWN", "not provable"));
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        service.reconcile(42L, "resolve unknown", "approver-b");

        verify(ownerPort, never()).execute(any());
        verify(repository).finishExecutionAndRequest(
                eq(42L), eq(6L), eq("CMD-UNKNOWN"), eq("approver-b"), eq(7L), eq("UNKNOWN"), eq("UNKNOWN"),
                eq("BAT-STILL-UNKNOWN"), eq("not provable"), eq(true), eq("approver-b"),
                eq("resolve unknown"), anyString(), eq(TRANSACTION_ID));
    }

    @Test
    void executeRejectsExpiredRequestBeforeReservation() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = supportedOwnerPort();
        when(repository.findRequest(42L)).thenReturn(Optional.of(approvedRequest(Instant.now().minusSeconds(1))));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("batOwner", ownerPort));

        assertThatThrownBy(() -> service.execute(42L, "approved maintenance", "approver-b"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("만료");

        verify(repository, never()).reserveExecution(anyLong(), anyLong(), anyString(), anyString());
        verify(ownerPort, never()).execute(any());
    }

    @Test
    void executeRejectsAmbiguousOwnerRoutingBeforeReservation() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort first = supportedOwnerPort();
        AdmApprovalOwnerCommandPort second = supportedOwnerPort();
        when(repository.findRequest(42L)).thenReturn(Optional.of(approvedRequest(Instant.now().plusSeconds(900))));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("first", first, "second", second));

        assertThatThrownBy(() -> service.execute(42L, "approved maintenance", "approver-b"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("둘 이상");

        verify(repository, never()).reserveExecution(anyLong(), anyLong(), anyString(), anyString());
        verify(first, never()).execute(any());
        verify(second, never()).execute(any());
    }

    private static AdmApprovalOwnerCommandPort supportedOwnerPort() {
        AdmApprovalOwnerCommandPort port = mock(AdmApprovalOwnerCommandPort.class);
        when(port.supports("BAT", "DRAIN", "DRAIN", "INSTANCE")).thenReturn(true);
        return port;
    }

    private static Map<String, Object> reserved(Map<String, Object> source) {
        Map<String, Object> reserved = new LinkedHashMap<>(source);
        reserved.put("leaseOwner", "approver-b");
        reserved.put("fenceToken", 7L);
        return reserved;
    }

    private static Map<String, Object> approvedRequest(Instant expireAt) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("approvalRequestId", 42L);
        request.put("requestKey", "REQ-EXEC-42");
        request.put("policyCode", "BAT-DRAIN");
        request.put("policyVersion", 1);
        request.put("actionType", "DRAIN");
        request.put("ownerModule", "BAT");
        request.put("ownerCommand", "DRAIN");
        request.put("targetType", "INSTANCE");
        request.put("targetId", "runtime-01");
        request.put("requestedBy", "requester-a");
        request.put("requestReason", "maintenance request");
        request.put("payloadSnapshot", PAYLOAD);
        request.put("approvalStatus", "APPROVED");
        request.put("currentStepNo", 1);
        request.put("expireAt", Timestamp.from(expireAt));
        request.put("transactionId", TRANSACTION_ID);
        request.put("versionNo", 3L);
        request.put("payloadHash", new AdmApprovalSnapshotIntegrity(new ObjectMapper()).hash(request));
        return request;
    }
}
