package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.approval.repository.BackofficeApprovalPolicyRepository;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackofficeApprovalPolicyServiceIdempotencyTest {
    private static final Instant DUE_AT = Instant.now().plusSeconds(3600);

    @Test
    void submitReturnsOnlyAnEquivalentReplay() {
        BackofficeApprovalPolicyRepository repository = replayRepository();
        BackofficeApprovalPolicyService service = newService(repository);

        service.submit(submitRequest("title", "{}", DUE_AT), "login-1");

        verify(repository, never()).insertPolicyApproval(org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void submitRejectsDivergentIdempotencyReplay() {
        BackofficeApprovalPolicyRepository repository = replayRepository();
        BackofficeApprovalPolicyService service = newService(repository);

        assertThatThrownBy(() -> service.submit(submitRequest("different", "{}", DUE_AT), "login-1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("재사용");
    }

    @Test
    void submitRejectsPastDueAtBeforeAnyInsert() {
        BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
        when(repository.findEmployeeNoByLoginId("login-1")).thenReturn(Optional.of("E-1"));
        BackofficeApprovalPolicyService service = newService(repository);

        assertThatThrownBy(() -> service.submit(
                submitRequest("title", "{}", Instant.now().minusSeconds(1)), "login-1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("dueAt");
        verify(repository, never()).insertPolicyApproval(org.mockito.ArgumentMatchers.anyMap());
    }

    @Test
    void decisionReplayMustBelongToTheSameApprovalAndActor() {
        BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
        when(repository.findEmployeeNoByLoginId("login-1")).thenReturn(Optional.of("E-1"));
        when(repository.participantDecisionExists("idem-decision")).thenReturn(true);
        when(repository.findParticipants(7L)).thenReturn(List.of(Map.of(
                "approvalId", 7L,
                "participantEmployeeNo", "E-1",
                "decisionStatus", "APPROVED",
                "decisionIdempotencyKey", "idem-decision",
                "decisionComment", "ok")));
        when(repository.findDocument(7L)).thenReturn(Optional.of(existingDocument()));
        when(repository.findLineStatuses(7L)).thenReturn(List.of());
        BackofficeApprovalPolicyService service = newService(repository);

        // 이 Test는 idempotencyKey replay 단락 경로만 검증하므로 아직 도달하지 않는
        // CAS(expectedVersionNo/expectedPayloadHash) 검증 값은 null로 둔다.
        service.decide(7L, new BackofficeApprovalPolicyService.DecisionRequest(
                "APPROVE", "idem-decision", "reason", "ok", null, null), "login-1");

        verify(repository, never()).decideParticipant(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void decisionKeyFromAnotherApprovalFailsClosed() {
        BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
        when(repository.findEmployeeNoByLoginId("login-1")).thenReturn(Optional.of("E-1"));
        when(repository.participantDecisionExists("idem-decision")).thenReturn(true);
        when(repository.findParticipants(7L)).thenReturn(List.of());
        BackofficeApprovalPolicyService service = newService(repository);

        assertThatThrownBy(() -> service.decide(7L, new BackofficeApprovalPolicyService.DecisionRequest(
                "APPROVE", "idem-decision", "reason", "ok", null, null), "login-1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("다른 결재");
    }

    @Test
    void ambiguousLifecycleReplayFailsClosedUntilHistoryOwnerQueryExists() {
        BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
        when(repository.historyActionExists("idem-life")).thenReturn(true);
        BackofficeApprovalPolicyService service = newService(repository);

        assertThatThrownBy(() -> service.withdraw(7L,
                new BackofficeApprovalPolicyService.LifecycleRequest("idem-life", "reason", "comment"), "login-1"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("안전하게");
    }

    /** Production 생성자와 동일하게 맞춘 공용 Test 조립 지점입니다. */
    static BackofficeApprovalPolicyService newService(BackofficeApprovalPolicyRepository repository) {
        ObjectMapper objectMapper = new ObjectMapper();
        return new BackofficeApprovalPolicyService(
                repository,
                objectMapper,
                new BackofficeApprovalDocumentAssembler(objectMapper),
                mock(BackofficeApprovalExecutionCoordinator.class));
    }

    private static BackofficeApprovalPolicyRepository replayRepository() {
        BackofficeApprovalPolicyRepository repository = mock(BackofficeApprovalPolicyRepository.class);
        when(repository.findEmployeeNoByLoginId("login-1")).thenReturn(Optional.of("E-1"));
        when(repository.findApprovalByIdempotencyKey("idem-submit")).thenReturn(Optional.of(7L));
        when(repository.findDocument(7L)).thenReturn(Optional.of(existingDocument()));
        when(repository.findParticipants(7L)).thenReturn(List.of());
        when(repository.findLineStatuses(7L)).thenReturn(List.of());
        return repository;
    }

    private static BackofficeApprovalPolicyService.SubmitRequest submitRequest(
            String title, String payload, Instant dueAt) {
        return new BackofficeApprovalPolicyService.SubmitRequest(
                "POL", 1, "DOM", "TYPE", null, title, "SEQUENTIAL", dueAt,
                payload, "ATT", "idem-submit", "reason");
    }

    private static Map<String,Object> existingDocument() {
        Map<String,Object> document = new HashMap<>();
        document.put("approvalId", 7L);
        document.put("requesterEmployeeNo", "E-1");
        document.put("title", "title");
        document.put("approvalMode", "SEQUENTIAL");
        document.put("payloadHash", sha256("{}"));
        document.put("attachmentGroupId", "ATT");
        document.put("policyCode", "POL");
        document.put("policyVersion", 1);
        document.put("businessDomain", "DOM");
        document.put("approvalType", "TYPE");
        document.put("dueAt", Timestamp.from(DUE_AT));
        document.put("approvalStatus", "IN_REVIEW");
        document.put("currentStepNo", 1);
        document.put("versionNo", 1L);
        document.put("transactionId", "tx-1");
        return document;
    }

    private static String sha256(String value) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
