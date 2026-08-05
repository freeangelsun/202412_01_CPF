package com.cpf.admin.approval.service;

import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.api.AdmApprovalTargetType;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AdmApprovalServiceIdempotencyTest {

    @Test
    void sameRequestKeyReplaysOnlyWhenImmutableRequestMatches() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        Instant expiry = Instant.now().plusSeconds(600);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-1", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-01", "{\"force\":false}", expiry, "maintenance");
        Map<String,Object> stored = storedRequest(expiry, batHash(42L, "runtime-01", "{\"force\":false}"));
        when(repository.findRequestIdByKey("REQ-1")).thenReturn(Optional.of(42L));
        when(repository.findRequest(42L)).thenReturn(Optional.of(stored));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.findExecution(42L)).thenReturn(Optional.empty());

        Map<String,Object> replay = service.requestApproval(request, "requester-a");

        assertThat(replay.get("approvalRequestId")).isEqualTo(42L);
        verify(repository, never()).insertRequest(anyMap());
    }

    @Test
    void sameRequestKeyRejectsDivergentPayloadOrTarget() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        Instant expiry = Instant.now().plusSeconds(600);
        when(repository.findRequestIdByKey("REQ-1")).thenReturn(Optional.of(42L));
        when(repository.findRequest(42L))
                .thenReturn(Optional.of(storedRequest(expiry, batHash(42L, "runtime-01", "{\"force\":false}"))));

        AdmApprovalService.CreateRequest divergent = new AdmApprovalService.CreateRequest(
                "REQ-1", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-02", "{\"force\":true}", expiry, "maintenance");

        assertThatThrownBy(() -> service.requestApproval(divergent, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다른");
        verify(repository, never()).insertRequest(anyMap());
    }

    @Test
    void decisionKeyReplaysOnlyForSameRequestActorActionAndReason() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        Map<String,Object> decision = Map.of(
                "approvalRequestId", 42L,
                "operatorId", "approver-b",
                "decisionStatus", "APPROVED",
                "decisionReason", "reviewed");
        when(repository.findDecisionByKey("DEC-1")).thenReturn(Optional.of(decision));
        when(repository.findRequest(42L)).thenReturn(Optional.of(storedRequest(
                Instant.now().plusSeconds(600), batHash(42L, "runtime-01", "{\"force\":false}"))));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.findExecution(42L)).thenReturn(Optional.empty());

        Map<String,Object> replay = service.decide(
                42L, new AdmApprovalService.DecisionRequest("APPROVE", "DEC-1", "reviewed"), "approver-b");

        assertThat(replay.get("approvalRequestId")).isEqualTo(42L);
        verify(repository, never()).decideParticipant(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void decisionKeyRejectsCrossRequestReuse() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        when(repository.findDecisionByKey("DEC-1")).thenReturn(Optional.of(Map.of(
                "approvalRequestId", 41L,
                "operatorId", "approver-b",
                "decisionStatus", "APPROVED",
                "decisionReason", "reviewed")));

        assertThatThrownBy(() -> service.decide(
                42L, new AdmApprovalService.DecisionRequest("APPROVE", "DEC-1", "reviewed"), "approver-b"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다른 승인 결정");
        verify(repository, never()).decideParticipant(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void requestRejectsAlreadyExpiredDeadline() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-1", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-01", "{}", Instant.now().minusSeconds(1), "maintenance");

        assertThatThrownBy(() -> service.requestApproval(request, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("현재 시각보다 뒤");
        verify(repository, never()).insertRequest(anyMap());
    }


    @Test
    void batRequestFinalizesCanonicalRiskFingerprintAfterGeneratedId() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        Instant expiry = Instant.now().plusSeconds(600);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-CC-1", "CENTER_CUT_REPROCESS_FAILED", 1,
                "CENTER_CUT_REPROCESS_FAILED", "BAT", "reprocessCenterCutFailed",
                "center_cut_execution", "EX-9", "{}", expiry, "incident recovery");
        when(repository.findRequestIdByKey("REQ-CC-1")).thenReturn(Optional.empty());
        when(repository.findPolicy("CENTER_CUT_REPROCESS_FAILED", 1)).thenReturn(Optional.of(Map.of(
                "policyCode", "CENTER_CUT_REPROCESS_FAILED", "policyVersion", 1,
                "actionType", "CENTER_CUT_REPROCESS_FAILED", "selfApprovalAllowedYn", "N")));
        when(repository.findPolicySteps("CENTER_CUT_REPROCESS_FAILED", 1)).thenReturn(List.of(Map.of(
                "stepNo", 1, "targetType", "ROLE", "targetCode", "CPF_ADMIN_APPROVER",
                "decisionRule", "ALL", "requiredYn", "Y")));
        when(repository.resolve(AdmApprovalTargetType.ROLE, "CPF_ADMIN_APPROVER", any(Instant.class)))
                .thenReturn(List.of(new AdmApprovalDirectoryEntry("approver-b", null, null, null)));
        when(repository.insertRequest(anyMap())).thenReturn(42L);
        when(repository.updateCommandSnapshot(eq(42L), eq(0L), anyString(), anyString(), eq("requester-a")))
                .thenReturn(1);
        when(repository.findRequest(42L)).thenReturn(Optional.of(storedRequest(
                expiry, batCenterCutHash(42L, "REQ-CC-1", "EX-9"))));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.findExecution(42L)).thenReturn(Optional.empty());

        service.requestApproval(request, "requester-a");

        org.mockito.ArgumentCaptor<String> hash = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> snapshot = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).updateCommandSnapshot(eq(42L), eq(0L), hash.capture(), snapshot.capture(), eq("requester-a"));
        assertThat(hash.getValue()).isEqualTo(batCenterCutHash(42L, "REQ-CC-1", "EX-9"));
        assertThat(snapshot.getValue()).contains("\"operation\":\"reprocessCenterCutFailed\"")
                .contains("\"approvalRequestId\":\"42\"")
                .contains("\"idempotencyKey\":\"REQ-CC-1\"");
    }

    @Test
    void batRequestRequiresExpiry() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), Map.of());
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-CC-1", "CENTER_CUT_REPROCESS_FAILED", 1,
                "CENTER_CUT_REPROCESS_FAILED", "BAT", "reprocessCenterCutFailed",
                "center_cut_execution", "EX-9", "{}", null, "incident recovery");

        assertThatThrownBy(() -> service.requestApproval(request, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("expireAt");
        verify(repository, never()).insertRequest(anyMap());
    }

    private static Map<String,Object> storedRequest(Instant expiry, String payloadHash) {
        return Map.ofEntries(
                Map.entry("approvalRequestId", 42L),
                Map.entry("requestKey", "REQ-1"),
                Map.entry("policyCode", "POLICY"),
                Map.entry("policyVersion", 1),
                Map.entry("actionType", "DRAIN"),
                Map.entry("ownerModule", "BAT"),
                Map.entry("ownerCommand", "DRAIN"),
                Map.entry("targetType", "INSTANCE"),
                Map.entry("targetId", "runtime-01"),
                Map.entry("requestedBy", "requester-a"),
                Map.entry("requestReason", "maintenance"),
                Map.entry("payloadHash", payloadHash),
                Map.entry("payloadSnapshot", "{\"force\":false}"),
                Map.entry("approvalStatus", "PENDING"),
                Map.entry("currentStepNo", 1),
                Map.entry("expireAt", Timestamp.from(expiry)),
                Map.entry("transactionId", "20260805000000000ADMapproval000001"),
                Map.entry("versionNo", 1L));
    }


    private static String batHash(long requestId, String targetId, String ignoredPayload) {
        return new CpfBatchRiskCommand(
                "DRAIN", "INSTANCE", targetId, "DRAIN", "requester-a", "maintenance",
                String.valueOf(requestId), "REQ-1", null, "").fingerprint();
    }

    private static String batCenterCutHash(long requestId, String requestKey, String executionId) {
        return new CpfBatchRiskCommand(
                "reprocessCenterCutFailed", "center_cut_execution", executionId,
                "CENTER_CUT_REPROCESS_FAILED", "requester-a", "incident recovery",
                String.valueOf(requestId), requestKey, null, "").fingerprint();
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    java.security.MessageDigest.getInstance("SHA-256")
                            .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
