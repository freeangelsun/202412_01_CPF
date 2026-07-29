package com.cpf.admin.approval.service;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdmApprovalServiceExecutionTest {
    private static final String TRANSACTION_ID =
            "20260729170000000" + "ADM" + "admAP01" + "0000001";

    @Test
    void executePropagatesOriginalRequesterAndAuthenticatedOperatorToOwnerPort() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort ownerPort = mock(AdmApprovalOwnerCommandPort.class);
        Map<String, Object> request = Map.ofEntries(
                Map.entry("approvalRequestId", 42L),
                Map.entry("actionType", "DRAIN"),
                Map.entry("ownerModule", "BAT"),
                Map.entry("ownerCommand", "DRAIN"),
                Map.entry("targetType", "INSTANCE"),
                Map.entry("targetId", "runtime-01"),
                Map.entry("requestedBy", "requester-a"),
                Map.entry("requestReason", "maintenance request"),
                Map.entry("payloadHash", "a".repeat(64)),
                Map.entry("approvalStatus", "APPROVED"),
                Map.entry("currentStepNo", 1),
                Map.entry("expireAt", Timestamp.from(Instant.now().plusSeconds(900))),
                Map.entry("transactionId", TRANSACTION_ID),
                Map.entry("versionNo", 3L));
        when(repository.findRequest(42L)).thenReturn(Optional.of(request));
        when(repository.findExecution(42L)).thenReturn(Optional.empty());
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.updateRequest(anyLong(), anyLong(), anyString(), anyInt(), anyString()))
                .thenReturn(1);
        when(ownerPort.execute(org.mockito.ArgumentMatchers.any(AdmApprovedOperationCommand.class)))
                .thenReturn(new AdmApprovedOperationResult(
                        AdmApprovalExecutionStatus.SUCCEEDED,
                        "BAT-SUCCEEDED",
                        "done"));
        AdmApprovalService service =
                new AdmApprovalService(repository, new ObjectMapper(), Map.of("BAT", ownerPort));

        service.execute(42L, "approved maintenance", "approver-b");

        ArgumentCaptor<AdmApprovedOperationCommand> command =
                ArgumentCaptor.forClass(AdmApprovedOperationCommand.class);
        verify(ownerPort).execute(command.capture());
        assertThat(command.getValue().requestedBy()).isEqualTo("requester-a");
        assertThat(command.getValue().approvedBy()).isEqualTo("approver-b");
        assertThat(command.getValue().reason()).isEqualTo("approved maintenance");
        assertThat(command.getValue().transactionId()).isEqualTo(TRANSACTION_ID);
    }
}
