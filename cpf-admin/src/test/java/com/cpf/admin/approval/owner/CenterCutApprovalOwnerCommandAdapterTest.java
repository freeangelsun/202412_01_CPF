package com.cpf.admin.approval.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.opr.centercut.AdmCenterCutCommandClient;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CenterCutApprovalOwnerCommandAdapterTest {
    private final AdmCenterCutCommandClient owner = mock(AdmCenterCutCommandClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CenterCutApprovalOwnerCommandAdapter adapter =
            new CenterCutApprovalOwnerCommandAdapter(owner, objectMapper);

    @Test
    void executesApprovedSnapshotAndPreservesOriginalRequesterAndReason() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "idem-201");
        when(owner.reprocessFailed(eq("EX-9"), any(CpfBatchRiskCommand.class)))
                .thenReturn(Map.of("executionId", "EX-9", "requeued", 3));

        var result = adapter.execute(command(risk, "approver-02", "execution audit reason"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        ArgumentCaptor<CpfBatchRiskCommand> sent = ArgumentCaptor.forClass(CpfBatchRiskCommand.class);
        verify(owner).reprocessFailed(eq("EX-9"), sent.capture());
        assertThat(sent.getValue().requestUser()).isEqualTo("requester-01");
        assertThat(sent.getValue().reason()).isEqualTo("incident recovery");
        assertThat(sent.getValue().fingerprint()).isEqualTo(risk.fingerprint());
    }

    @Test
    void rejectsSelfApprovalAndSnapshotMismatchBeforeOwnerCall() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "idem-201");
        assertThat(adapter.execute(command(risk, "requester-01", "audit")).status())
                .isEqualTo(AdmApprovalExecutionStatus.FAILED);

        AdmApprovedOperationCommand valid = command(risk, "approver-02", "audit");
        AdmApprovedOperationCommand mismatch = new AdmApprovedOperationCommand(
                valid.approvalRequestId(), valid.commandRequestId(), valid.actionType(),
                valid.ownerModule(), valid.ownerCommand(), valid.targetType(), "EX-10",
                valid.payloadHash(), valid.payloadSnapshot(), valid.requestedBy(),
                valid.approvedBy(), valid.reason(), valid.transactionId());
        assertThat(adapter.execute(mismatch).status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        verify(owner, never()).reprocessFailed(any(), any());
    }

    @Test
    void preservesUnknownForReconciliation() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "idem-201");
        when(owner.reprocessFailed(eq("EX-9"), any(CpfBatchRiskCommand.class)))
                .thenThrow(new CpfBatchOwnerUnknownResultException("CENTER_UNKNOWN", "timeout"));

        var result = adapter.execute(command(risk, "approver-02", "audit"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
        assertThat(result.resultCode()).isEqualTo("CENTER_UNKNOWN");
    }

    private AdmApprovedOperationCommand command(
            CpfBatchRiskCommand risk, String approvedBy, String executionReason) throws Exception {
        return new AdmApprovedOperationCommand(
                201L, "ADM-APP-201-1", risk.actionType(), "BAT", risk.operation(),
                risk.targetType(), risk.targetId(), risk.fingerprint(),
                objectMapper.writeValueAsString(risk), risk.requestUser(), approvedBy,
                executionReason, "TX-201");
    }

    private static CpfBatchRiskCommand risk(String requester, String requestKey) {
        return new CpfBatchRiskCommand(
                "reprocessCenterCutFailed", "center_cut_execution", "EX-9",
                "CENTER_CUT_REPROCESS_FAILED", requester, "incident recovery",
                "201", requestKey, null, "");
    }
}
