package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalCapabilityNonceRepository;
import com.cpf.admin.approval.security.AdmDataQualityCorrectionGateway;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.security.AdmDataQualityApprovalProofService;
import com.cpf.data.api.quality.CpfDataQualityOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DataQualityCorrectionApprovalOwnerCommandAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AdmApprovalSnapshotIntegrity integrity = new AdmApprovalSnapshotIntegrity(objectMapper);
    private final AdmDataQualityCorrectionGateway correction = mock(AdmDataQualityCorrectionGateway.class);
    private final CpfDataQualityOperations query = mock(CpfDataQualityOperations.class);
    private final AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
    private final AdmApprovalCapabilityNonceRepository nonceRepository =
            mock(AdmApprovalCapabilityNonceRepository.class);
    private final AdmDataQualityApprovalProofService proofService = new AdmDataQualityApprovalProofService(
            Base64.getEncoder().encodeToString(new byte[32]),
            nonceRepository,
            Duration.ofMinutes(15),
            Clock.systemUTC());
    private final DataQualityCorrectionApprovalOwnerCommandAdapter adapter =
            new DataQualityCorrectionApprovalOwnerCommandAdapter(correction, query, objectMapper, repository, integrity, proofService);

    @Test
    void directOwnerAdapterCallWithoutServerReservationFailsClosed() {
        AdmApprovedOperationCommand command = command("0".repeat(64));
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.empty());

        assertThat(adapter.execute(command).status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        verifyNoInteractions(correction);
    }

    @Test
    void hashMutationFailsBeforeOwnerMutation() {
        Map<String, Object> reserved = reserved();
        reserved.put("payloadHash", "f".repeat(64));
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));

        assertThat(adapter.execute(command("f".repeat(64))).resultCode()).isEqualTo("DQ-EXECUTION-ENVELOPE-MISMATCH");
        verifyNoInteractions(correction);
    }

    @Test
    void callerCannotReuseReservedHashWithMutatedPayloadSnapshot() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved);
        reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));

        AdmApprovedOperationCommand mutated = new AdmApprovedOperationCommand(
                41L, "CMD-1", "DATA_QUALITY_CORRECTION", "CMN", "correctQuarantine",
                "DATA_QUALITY_QUARANTINE", "DQ-1", hash,
                "{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"attacker\"}}",
                "maker", "checker", "approved correction", "TX-1");

        assertThat(adapter.execute(mutated).resultCode()).isEqualTo("DQ-EXECUTION-ENVELOPE-MISMATCH");
        verifyNoInteractions(correction);
    }

    @Test
    void validReservedCommandMutatesExactlyOnce() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved);
        reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));
        when(repository.isApprovedParticipant(41L, "checker")).thenReturn(true);
        CpfDataQualityOperations.QuarantineItem before = new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of(), "QUARANTINED", 2, List.of());
        CpfDataQualityOperations.QuarantineItem after = new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of("name", "new"), "CORRECTED", 3, List.of());
        when(query.quarantine("DQ-1")).thenReturn(Optional.of(before));
        when(correction.correctApproved(any())).thenReturn(after);

        assertThat(adapter.execute(command(hash)).status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        verify(correction, times(1)).correctApproved(any());
    }

    @Test
    void reconcileConfirmsAppliedSideEffectWithoutCallingMutationAgain() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved); reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));
        when(repository.isApprovedParticipant(41L, "checker")).thenReturn(true);
        when(query.quarantine("DQ-1")).thenReturn(Optional.of(new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of("name", "new"), "CORRECTED", 3, List.of())));

        assertThat(adapter.reconcile(command(hash)).status()).isEqualTo(AdmApprovalExecutionStatus.RECOVERED);
        verifyNoInteractions(correction);
    }

    @Test
    void reconcileConfirmsNotAppliedWithoutCallingMutationAgain() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved); reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));
        when(repository.isApprovedParticipant(41L, "checker")).thenReturn(true);
        when(query.quarantine("DQ-1")).thenReturn(Optional.of(new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of(), "QUARANTINED", 2, List.of())));

        assertThat(adapter.reconcile(command(hash)).resultCode()).isEqualTo("DQ-CORRECTION-NOT-APPLIED");
        verifyNoInteractions(correction);
    }

    @Test
    void reconcileKeepsUnknownForAmbiguousOwnerState() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved); reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));
        when(repository.isApprovedParticipant(41L, "checker")).thenReturn(true);
        when(query.quarantine("DQ-1")).thenReturn(Optional.of(new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of("name", "other"), "CORRECTED", 9, List.of())));

        assertThat(adapter.reconcile(command(hash)).status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
        verifyNoInteractions(correction);
    }


    @Test
    void reconcileKeepsUnknownWhenMatchingPayloadHasAdvancedBeyondSingleApprovedVersion() {
        Map<String, Object> reserved = reserved();
        String hash = integrity.hash(reserved); reserved.put("payloadHash", hash);
        when(repository.findReservedExecutionCommand(41L, "CMD-1")).thenReturn(Optional.of(reserved));
        when(repository.isApprovedParticipant(41L, "checker")).thenReturn(true);
        when(query.quarantine("DQ-1")).thenReturn(Optional.of(new CpfDataQualityOperations.QuarantineItem(
                "DQ-1", "R-1", Map.of("name", "old"), Map.of("name", "new"), "CORRECTED", 4, List.of())));

        assertThat(adapter.reconcile(command(hash)).status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
        verifyNoInteractions(correction);
    }

    private AdmApprovedOperationCommand command(String hash) {
        return new AdmApprovedOperationCommand(
                41L, "CMD-1", "DATA_QUALITY_CORRECTION", "CMN", "correctQuarantine",
                "DATA_QUALITY_QUARANTINE", "DQ-1", hash,
                "{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"new\"}}",
                "maker", "checker", "approved correction", "TX-1");
    }

    private Map<String, Object> reserved() {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("approvalRequestId", 41L);
        value.put("commandRequestId", "CMD-1");
        value.put("requestKey", "REQ-1");
        value.put("policyCode", "DQ-CORRECTION");
        value.put("policyVersion", 1);
        value.put("actionType", "DATA_QUALITY_CORRECTION");
        value.put("ownerModule", "CMN");
        value.put("ownerCommand", "correctQuarantine");
        value.put("targetType", "DATA_QUALITY_QUARANTINE");
        value.put("targetId", "DQ-1");
        value.put("requestedBy", "maker");
        value.put("requestReason", "approved correction");
        value.put("expireAt", Instant.now().plusSeconds(300));
        value.put("transactionId", "TX-1");
        value.put("payloadSnapshot", "{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"new\"}}");
        return value;
    }
}
