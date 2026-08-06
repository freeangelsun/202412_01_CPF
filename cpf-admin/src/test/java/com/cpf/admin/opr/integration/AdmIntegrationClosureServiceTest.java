package com.cpf.admin.opr.integration;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.core.api.data.quality.CpfDataQualityOperations;
import com.cpf.core.api.security.crypto.CpfCryptoOperations;
import com.cpf.core.api.time.CpfTimeOperations;
import com.cpf.core.api.webhook.CpfWebhookOperations;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AdmIntegrationClosureServiceTest {
    private final CpfDataQualityOperations quality = mock(CpfDataQualityOperations.class);
    private final CpfTimeOperations time = mock(CpfTimeOperations.class);
    private final CpfWebhookOperations webhook = mock(CpfWebhookOperations.class);
    private final AdmApprovalService approvals = mock(AdmApprovalService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private AdmIntegrationClosureService service() {
        return new AdmIntegrationClosureService(null, quality, time, webhook, approvals, objectMapper, Duration.ofMinutes(15));
    }

    @Test
    void correctionRequestStoresImmutableSnapshotWithoutClientApprovalFlag() throws Exception {
        Instant now = Instant.parse("2026-08-06T03:00:00Z");
        when(time.now()).thenReturn(now);
        when(approvals.requestApproval(any(), eq("maker"))).thenReturn(Map.of("approvalRequestId", 77L));
        Map<String,Object> result = service().requestCorrection(
                "DQ-1", 3, Map.of("name", "Kim"), "idem-1", "maker", "fix invalid name");
        assertThat(result).containsEntry("approvalRequestId", 77L);
        ArgumentCaptor<AdmApprovalService.CreateRequest> request = ArgumentCaptor.forClass(AdmApprovalService.CreateRequest.class);
        verify(approvals).requestApproval(request.capture(), eq("maker"));
        Map<String,Object> snapshot = objectMapper.readValue(request.getValue().payloadSnapshot(), new TypeReference<>() {});
        assertThat(snapshot).containsEntry("quarantineId", "DQ-1").containsEntry("expectedVersion", 3);
        assertThat(snapshot).doesNotContainKey("approved");
        assertThat(request.getValue().expireAt()).isEqualTo(now.plus(Duration.ofMinutes(15)));
    }

    @Test
    void executionRequiresApprovedParticipantDifferentFromMakerAndMatchingTarget() {
        Instant now=Instant.parse("2026-08-06T03:00:00Z");
        when(time.now()).thenReturn(now);
        when(approvals.detail(77L)).thenReturn(Map.of(
                "actionType", AdmIntegrationClosureService.DATA_QUALITY_ACTION,
                "ownerModule", AdmIntegrationClosureService.DATA_QUALITY_OWNER,
                "ownerCommand", AdmIntegrationClosureService.DATA_QUALITY_COMMAND,
                "targetType", AdmIntegrationClosureService.DATA_QUALITY_TARGET,
                "approvalStatus", "APPROVED",
                "requestedBy", "maker",
                "expireAt", Timestamp.from(now.plusSeconds(60)),
                "participants", List.of(Map.of("operatorId","checker","decisionStatus","APPROVED"))));
        when(approvals.execute(77L,"execute","checker")).thenReturn(Map.of(
                "approvalRequestId",77L,"payloadSnapshot","secret",
                "execution",Map.of("resultCode","DQ-CORRECTED","payloadSnapshot","secret")));
        Map<String,Object> result=service().executeCorrection(77L,"checker","execute");
        assertThat(result).doesNotContainKey("payloadSnapshot");
        assertThat((Map<?,?>)result.get("execution")).doesNotContainKey("payloadSnapshot");
        verify(approvals).execute(77L,"execute","checker");
    }

    @Test
    void makerCannotExecuteAndExpiredApprovalIsRejected() {
        Instant now=Instant.parse("2026-08-06T03:00:00Z"); when(time.now()).thenReturn(now);
        Map<String,Object> base=Map.of(
                "actionType",AdmIntegrationClosureService.DATA_QUALITY_ACTION,
                "ownerModule",AdmIntegrationClosureService.DATA_QUALITY_OWNER,
                "ownerCommand",AdmIntegrationClosureService.DATA_QUALITY_COMMAND,
                "targetType",AdmIntegrationClosureService.DATA_QUALITY_TARGET,
                "approvalStatus","APPROVED","requestedBy","maker",
                "expireAt",Timestamp.from(now.minusSeconds(1)),
                "participants",List.of(Map.of("operatorId","checker","decisionStatus","APPROVED")));
        when(approvals.detail(77L)).thenReturn(base);
        assertThatThrownBy(() -> service().executeCorrection(77L,"maker","execute")).isInstanceOf(RuntimeException.class);
        assertThatThrownBy(() -> service().executeCorrection(77L,"checker","execute")).isInstanceOf(RuntimeException.class);
        verify(approvals,never()).execute(anyLong(),anyString(),anyString());
    }

    @Test
    void cryptoStatusNeverExposesKeyMaterial() {
        CpfCryptoOperations crypto=mock(CpfCryptoOperations.class); when(crypto.activeKeyVersion()).thenReturn("kms-v9");
        AdmIntegrationClosureService s=new AdmIntegrationClosureService(crypto,quality,time,webhook,approvals,objectMapper,Duration.ofMinutes(15));
        assertThat(s.cryptoStatus()).containsEntry("configured",true).containsEntry("activeKeyVersion","kms-v9").containsEntry("plaintextKeyExposed",false);
    }
}
