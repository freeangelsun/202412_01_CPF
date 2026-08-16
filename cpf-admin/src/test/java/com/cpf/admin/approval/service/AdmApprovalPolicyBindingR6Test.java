package com.cpf.admin.approval.service;

import com.cpf.admin.approval.repository.AdmApprovalRepository;
import com.cpf.admin.approval.security.AdmApprovalSnapshotIntegrity;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdmApprovalPolicyBindingR6Test {
    private static final String ACTION = "DATA_QUALITY_CORRECTION";

    @Test
    void explicitOldPolicyCannotOverrideCanonicalActivePolicy() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort owner = mock(AdmApprovalOwnerCommandPort.class);
        when(owner.supports("ADM", "correctDataQuality", ACTION, "DATA_QUALITY_QUARANTINE")).thenReturn(true);
        when(repository.findActivePolicy(eq(ACTION), any(Instant.class))).thenReturn(Optional.of(policy("DQ", 2, "Y")));
        when(repository.findPolicy("DQ", 1)).thenReturn(Optional.of(policy("DQ", 1, "Y")));
        AdmApprovalService service = service(repository, owner);

        assertThrows(AdmApprovalConflictException.class, () -> service.requestApproval(request("DQ", 1), "requester"));
        verify(repository, never()).insertRequest(anyMap());
    }

    @Test
    void disabledCanonicalPolicyIsFailClosed() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort owner = mock(AdmApprovalOwnerCommandPort.class);
        when(owner.supports(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(repository.findActivePolicy(eq(ACTION), any(Instant.class))).thenReturn(Optional.of(policy("DQ", 2, "N")));
        AdmApprovalService service = service(repository, owner);

        assertThrows(CpfValidationException.class, () -> service.requestApproval(request(null, null), "requester"));
        verify(repository, never()).insertRequest(anyMap());
    }

    @Test
    void unknownOwnerActionTupleIsRejectedBeforePolicyLookup() {
        AdmApprovalRepository repository = mock(AdmApprovalRepository.class);
        AdmApprovalOwnerCommandPort owner = mock(AdmApprovalOwnerCommandPort.class);
        when(owner.supports(anyString(), anyString(), anyString(), anyString())).thenReturn(false);
        AdmApprovalService service = service(repository, owner);

        assertThrows(CpfValidationException.class, () -> service.requestApproval(request(null, null), "requester"));
        verify(repository, never()).findActivePolicy(anyString(), any(Instant.class));
    }

    private static AdmApprovalService service(AdmApprovalRepository repository, AdmApprovalOwnerCommandPort owner) {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        return new AdmApprovalService(repository, mapper, new AdmApprovalSnapshotIntegrity(mapper), Map.of("dq", owner));
    }

    private static Map<String,Object> policy(String code, int version, String enabled) {
        return Map.of(
                "policyCode", code,
                "policyVersion", version,
                "actionType", ACTION,
                "enabledYn", enabled,
                "effectiveFrom", Instant.now().minusSeconds(60));
    }

    private static AdmApprovalService.CreateRequest request(String policyCode, Integer policyVersion) {
        return new AdmApprovalService.CreateRequest(
                "request-key-0001", policyCode, policyVersion, ACTION,
                "ADM", "correctDataQuality", "DATA_QUALITY_QUARANTINE", "DQ-1",
                "{\"quarantineId\":\"DQ-1\",\"expectedVersion\":1,\"corrected\":{\"name\":\"fixed\"}}",
                Instant.now().plusSeconds(300), "valid reason");
    }
}
