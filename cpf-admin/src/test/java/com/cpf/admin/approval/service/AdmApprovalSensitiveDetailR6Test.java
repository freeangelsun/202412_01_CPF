package com.cpf.admin.approval.service;

import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdmApprovalSensitiveDetailR6Test {
    @Test
    void externalDetailOmitsPayloadAndMasksExecutionMessage() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        when(repository.findRequest(7L)).thenReturn(Optional.of(Map.of(
                "approvalRequestId", 7L,
                "targetId", "DQ-7",
                "payloadSnapshot", "{\"password\":\"raw-secret\"}",
                "secretToken", "raw-secret",
                "transactionId", "tx-7")));
        when(repository.findParticipants(7L)).thenReturn(List.of(Map.of(
                "participantId", 1L, "operatorId", "approver", "decisionStatus", "APPROVED",
                "idempotencyKey", "must-not-leak")));
        when(repository.findExecution(7L)).thenReturn(Optional.of(Map.of(
                "commandRequestId", "cmd-7", "executionStatus", "SUCCEEDED",
                "ownerResultMessage", "token=raw-secret")));
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        AdmApprovalService service = new AdmApprovalService(repository, mapper,
                new AdmApprovalSnapshotIntegrity(mapper), Map.of());

        Map<String,Object> detail = service.detail(7L);
        assertFalse(detail.containsKey("payloadSnapshot"));
        assertFalse(detail.containsKey("secretToken"));
        assertEquals("tx-7", detail.get("transactionId"));
        @SuppressWarnings("unchecked")
        List<Map<String,Object>> participants = (List<Map<String,Object>>) detail.get("participants");
        assertFalse(participants.getFirst().containsKey("idempotencyKey"));
        @SuppressWarnings("unchecked")
        Map<String,Object> execution = (Map<String,Object>) detail.get("execution");
        assertEquals("token=***", execution.get("ownerResultMessage"));
        assertFalse(detail.toString().contains("raw-secret"));
    }
}
