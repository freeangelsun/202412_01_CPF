package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.approval.api.AdmApprovedOperationResult;
import com.cpf.common.data.quality.InMemoryCpfDataQualityOperations;
import com.cpf.core.api.data.quality.CpfDataQualityDecision;
import com.cpf.core.api.data.quality.CpfDataQualityRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DataQualityCorrectionApprovalOwnerCommandAdapterTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private InMemoryCpfDataQualityOperations quality;
    private DataQualityCorrectionApprovalOwnerCommandAdapter adapter;
    private String quarantineId;

    @BeforeEach
    void setUp() {
        quality = new InMemoryCpfDataQualityOperations();
        quality.register(new CpfDataQualityRule(
                "NAME_REQUIRED", 1, "name", "NOT_BLANK",
                CpfDataQualityRule.Severity.ERROR,
                CpfDataQualityRule.State.ACTIVE,
                Map.of()), "policy-admin", "activate rule");
        CpfDataQualityDecision rejected = quality.validate("REC-1", Map.of("name", ""));
        quarantineId = rejected.quarantineId();
        adapter = new DataQualityCorrectionApprovalOwnerCommandAdapter(quality, objectMapper);
    }

    @Test
    void immutableApprovedSnapshotCorrectsOnceWithAuditHashes() throws Exception {
        AdmApprovedOperationResult result = adapter.execute(command(
                quarantineId, 1, "maker", "checker", Map.of("name", "Kim")));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        assertThat(result.resultCode()).isEqualTo("DQ-CORRECTED");
        assertThat(result.maskedMessage()).contains("approvalId=77", "beforeHash=", "afterHash=");
        assertThat(quality.quarantine(quarantineId).orElseThrow().state()).isEqualTo("CORRECTED");
    }

    @Test
    void targetSubstitutionIsRejected() throws Exception {
        AdmApprovedOperationResult result = adapter.execute(command(
                "DQ-OTHER", 1, "maker", "checker", Map.of("name", "Kim")));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("DQ-NOT-FOUND");
    }

    @Test
    void makerCheckerViolationIsRejected() throws Exception {
        AdmApprovedOperationResult result = adapter.execute(command(
                quarantineId, 1, "same-user", "same-user", Map.of("name", "Kim")));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("DQ-SOD-VIOLATION");
    }

    @Test
    void staleOptimisticVersionIsRejected() throws Exception {
        AdmApprovedOperationResult result = adapter.execute(command(
                quarantineId, 9, "maker", "checker", Map.of("name", "Kim")));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        assertThat(result.resultCode()).isEqualTo("DQ-VERSION-CONFLICT");
    }

    private AdmApprovedOperationCommand command(
            String targetId,
            long expectedVersion,
            String requestedBy,
            String approvedBy,
            Map<String, Object> corrected) throws Exception {
        String payload = objectMapper.writeValueAsString(Map.of(
                "quarantineId", targetId,
                "expectedVersion", expectedVersion,
                "corrected", corrected));
        return new AdmApprovedOperationCommand(
                77,
                "CMD-77",
                "DATA_QUALITY_CORRECTION",
                "CMN",
                "correctQuarantine",
                "DATA_QUALITY_QUARANTINE",
                targetId,
                "a".repeat(64),
                payload,
                requestedBy,
                approvedBy,
                "approved correction",
                "TX-77");
    }
}
