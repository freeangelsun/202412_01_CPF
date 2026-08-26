package com.cpf.admin.approval.service;

import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.spi.AdmApprovalDirectoryEntry;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.admin.approval.api.AdmApprovalTargetType;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdmApprovalServiceIdempotencyTest {

    @Test
    void sameRequestKeyReplaysOnlyWhenImmutableRequestMatches() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = requestService(repository);
        Instant expiry = Instant.now().plusSeconds(600);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-0001", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-01", "{\"force\":false}", expiry, "maintenance");
        Map<String,Object> stored = storedRequest(expiry, batHash(42L, "runtime-01", "{\"force\":false}"));
        when(repository.findRequestIdByKey("REQ-0001")).thenReturn(Optional.of(42L));
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
        AdmApprovalService service = requestService(repository);
        Instant expiry = Instant.now().plusSeconds(600);
        when(repository.findRequestIdByKey("REQ-0001")).thenReturn(Optional.of(42L));
        when(repository.findRequest(42L))
                .thenReturn(Optional.of(storedRequest(expiry, batHash(42L, "runtime-01", "{\"force\":false}"))));

        AdmApprovalService.CreateRequest divergent = new AdmApprovalService.CreateRequest(
                "REQ-0001", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-02", "{\"force\":true}", expiry, "maintenance");

        assertThatThrownBy(() -> service.requestApproval(divergent, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다른");
        verify(repository, never()).insertRequest(anyMap());
    }

    @Test
    void decisionKeyReplaysOnlyForSameRequestActorActionAndReason() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), new AdmApprovalSnapshotIntegrity(new ObjectMapper()), Map.of());
        Map<String,Object> decision = Map.of(
                "approvalRequestId", 42L,
                "operatorId", "approver-b",
                "decisionStatus", "APPROVED",
                "decisionReason", "reviewed");
        when(repository.findDecisionByKey("DEC-0001")).thenReturn(Optional.of(decision));
        when(repository.findRequest(42L)).thenReturn(Optional.of(storedRequest(
                Instant.now().plusSeconds(600), batHash(42L, "runtime-01", "{\"force\":false}"))));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.findExecution(42L)).thenReturn(Optional.empty());

        Map<String,Object> replay = service.decide(
                42L, new AdmApprovalService.DecisionRequest("APPROVE", "DEC-0001", "reviewed"), "approver-b");

        assertThat(replay.get("approvalRequestId")).isEqualTo(42L);
        verify(repository, never()).decideParticipant(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void decisionKeyRejectsCrossRequestReuse() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = new AdmApprovalService(repository, new ObjectMapper(), new AdmApprovalSnapshotIntegrity(new ObjectMapper()), Map.of());
        when(repository.findDecisionByKey("DEC-0001")).thenReturn(Optional.of(Map.of(
                "approvalRequestId", 41L,
                "operatorId", "approver-b",
                "decisionStatus", "APPROVED",
                "decisionReason", "reviewed")));

        assertThatThrownBy(() -> service.decide(
                42L, new AdmApprovalService.DecisionRequest("APPROVE", "DEC-0001", "reviewed"), "approver-b"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다른 승인 결정");
        verify(repository, never()).decideParticipant(anyLong(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void requestRejectsAlreadyExpiredDeadline() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = requestService(repository);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-0001", "POLICY", 1, "DRAIN", "BAT", "DRAIN",
                "INSTANCE", "runtime-01", "{}", Instant.now().minusSeconds(1), "maintenance");

        assertThatThrownBy(() -> service.requestApproval(request, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("현재 시각보다 뒤");
        verify(repository, never()).insertRequest(anyMap());
    }


    @Test
    void batRequestFinalizesCanonicalRiskFingerprintAfterGeneratedId() throws Exception {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = requestService(repository);
        Instant expiry = Instant.now().plusSeconds(600);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-CC-1", "CENTER_CUT_REPROCESS_FAILED", 1,
                "CENTER_CUT_REPROCESS_FAILED", "BAT", "reprocessCenterCutFailed",
                "center_cut_execution", "EX-9", "{}", expiry, "incident recovery");
        when(repository.findRequestIdByKey("REQ-CC-1")).thenReturn(Optional.empty());
        Map<String,Object> policy = Map.of(
                "policyCode", "CENTER_CUT_REPROCESS_FAILED", "policyVersion", 1,
                "actionType", "CENTER_CUT_REPROCESS_FAILED", "enabledYn", "Y",
                "selfApprovalAllowedYn", "N");
        when(repository.findActivePolicy(eq("CENTER_CUT_REPROCESS_FAILED"), any(Instant.class)))
                .thenReturn(Optional.of(policy));
        when(repository.findPolicy("CENTER_CUT_REPROCESS_FAILED", 1)).thenReturn(Optional.of(policy));
        when(repository.findPolicySteps("CENTER_CUT_REPROCESS_FAILED", 1)).thenReturn(List.of(Map.of(
                "stepNo", 1, "targetType", "ROLE", "targetCode", "CPF_ADMIN_APPROVER",
                "decisionRule", "ALL", "requiredYn", "Y")));
        when(repository.resolve(eq(AdmApprovalTargetType.ROLE), eq("CPF_ADMIN_APPROVER"), any(Instant.class)))
                .thenReturn(List.of(new AdmApprovalDirectoryEntry("approver-b", null, null, null)));
        AtomicReference<Map<String,Object>> insertedRequest = new AtomicReference<>();
        doAnswer(invocation -> {
            insertedRequest.set(invocation.getArgument(0));
            return 42L;
        }).when(repository).insertRequest(anyMap());
        when(repository.updateCommandSnapshot(eq(42L), eq(0L), anyString(), anyString(), eq("requester-a")))
                .thenReturn(1);
        when(repository.findRequest(42L)).thenReturn(Optional.of(storedRequest(
                expiry, batCenterCutHash(42L, "REQ-CC-1", "EX-9"))));
        when(repository.findParticipants(42L)).thenReturn(List.of());
        when(repository.findExecution(42L)).thenReturn(Optional.empty());

        try (AutoCloseable _ = bindContext()) {
            service.requestApproval(request, "requester-a");
        }

        org.mockito.ArgumentCaptor<String> hash = org.mockito.ArgumentCaptor.forClass(String.class);
        org.mockito.ArgumentCaptor<String> snapshot = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).insertRequest(anyMap());
        verify(repository).updateCommandSnapshot(eq(42L), eq(0L), hash.capture(), snapshot.capture(), eq("requester-a"));
        Map<String,Object> expectedEnvelope = new LinkedHashMap<>(java.util.Objects.requireNonNull(insertedRequest.get()));
        expectedEnvelope.put("payloadSnapshot", snapshot.getValue());
        assertThat(hash.getValue()).isEqualTo(new AdmApprovalSnapshotIntegrity(new ObjectMapper()).hash(expectedEnvelope));
        assertThat(snapshot.getValue()).contains("\"operation\":\"reprocessCenterCutFailed\"")
                .contains("\"approvalRequestId\":\"42\"")
                .contains("\"idempotencyKey\":\"REQ-CC-1\"");
    }

    @Test
    void batRequestRequiresExpiry() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalService service = requestService(repository);
        AdmApprovalService.CreateRequest request = new AdmApprovalService.CreateRequest(
                "REQ-CC-1", "CENTER_CUT_REPROCESS_FAILED", 1,
                "CENTER_CUT_REPROCESS_FAILED", "BAT", "reprocessCenterCutFailed",
                "center_cut_execution", "EX-9", "{}", null, "incident recovery");

        assertThatThrownBy(() -> service.requestApproval(request, "requester-a"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("expireAt");
        verify(repository, never()).insertRequest(anyMap());
    }

    private static Map<String,Object> storedRequest(Instant expiry, String ignoredLegacyHash) {
        Map<String,Object> stored = new LinkedHashMap<>();
        stored.put("approvalRequestId", 42L);
        stored.put("requestKey", "REQ-0001");
        stored.put("policyCode", "POLICY");
        stored.put("policyVersion", 1);
        stored.put("actionType", "DRAIN");
        stored.put("ownerModule", "BAT");
        stored.put("ownerCommand", "DRAIN");
        stored.put("targetType", "INSTANCE");
        stored.put("targetId", "runtime-01");
        stored.put("requestedBy", "requester-a");
        stored.put("requestReason", "maintenance");
        stored.put("payloadSnapshot", "{\"operation\":\"DRAIN\",\"targetType\":\"INSTANCE\",\"targetId\":\"runtime-01\",\"actionType\":\"DRAIN\",\"requestUser\":\"requester-a\",\"reason\":\"maintenance\",\"approvalRequestId\":\"42\",\"idempotencyKey\":\"REQ-0001\",\"expectedVersion\":null,\"payload\":\"\"}");
        stored.put("approvalStatus", "PENDING");
        stored.put("currentStepNo", 1);
        stored.put("expireAt", Timestamp.from(expiry));
        stored.put("transactionId", "20260805000000000ADMapproval000001");
        stored.put("versionNo", 1L);
        stored.put("payloadHash", new AdmApprovalSnapshotIntegrity(new ObjectMapper()).hash(stored));
        return stored;
    }

    private static String batHash(long requestId, String targetId, String ignoredPayload) {
        return "legacy-hash-not-used";
    }

    private static String batCenterCutHash(long requestId, String requestKey, String executionId) {
        return "legacy-hash-not-used";
    }

    private static AdmApprovalService requestService(AdmApprovalRepository repository) {
        AdmApprovalOwnerCommandPort owner = mock(AdmApprovalOwnerCommandPort.class);
        when(owner.supports("BAT", "DRAIN", "DRAIN", "INSTANCE")).thenReturn(true);
        when(owner.supports(
                "BAT", "reprocessCenterCutFailed",
                "CENTER_CUT_REPROCESS_FAILED", "center_cut_execution"))
                .thenReturn(true);
        return new AdmApprovalService(repository, new ObjectMapper(), new AdmApprovalSnapshotIntegrity(new ObjectMapper()), Map.of("batOwner", owner));
    }

    private static AutoCloseable bindContext() {
        Instant now = Instant.parse("2026-08-22T00:00:00Z");
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(
                () -> "TX-ADM-APPROVAL-TEST",
                new CpfExecutionIdGenerator() {
                    @Override public String newExecutionId() { return "EX-ADM-APPROVAL-TEST"; }
                    @Override public String newSegmentId() { return "SG-ADM-APPROVAL-TEST"; }
                },
                (CpfBusinessDateProvider) () -> LocalDate.of(2026, 8, 22),
                Clock.fixed(now, ZoneOffset.UTC));
        CpfContext context = factory.newRoot(
                null,
                "adm.approval.test",
                new CpfContext.CpfIdentityContext(
                        "requester-a", "requester-a", CpfContext.CpfPrincipalType.OPERATOR),
                null,
                now.plusSeconds(600));
        return CpfContexts.bind(CpfContextSnapshot.capture(context, now));
    }
}
