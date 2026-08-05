package com.cpf.admin.approval.owner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.core.api.batch.CpfBatchOperationsPort;
import com.cpf.core.api.batch.CpfBatchOwnerUnknownResultException;
import com.cpf.core.api.batch.CpfBatchRiskCommand;
import com.cpf.core.api.data.CpfDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class BatchRuntimeApprovalOwnerCommandAdapterTest {
    private final CpfBatchOperationsPort batch = mock(CpfBatchOperationsPort.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final BatchRuntimeApprovalOwnerCommandAdapter adapter =
            new BatchRuntimeApprovalOwnerCommandAdapter(batch, objectMapper);

    @Test
    void executesOnlyTheApprovedSnapshotAndPreservesRequesterIdentity() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "CMD-201");
        when(batch.requestRetry(eq(42L), any(CpfBatchRiskCommand.class)))
                .thenReturn(mock(CpfDataRow.class));

        var result = adapter.execute(command(risk, "approver-02"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        ArgumentCaptor<CpfBatchRiskCommand> sent = ArgumentCaptor.forClass(CpfBatchRiskCommand.class);
        verify(batch).requestRetry(eq(42L), sent.capture());
        assertThat(sent.getValue().requestUser()).isEqualTo("requester-01");
        assertThat(sent.getValue().fingerprint()).isEqualTo(risk.fingerprint());
    }

    @Test
    void rejectsSelfApprovalAndSnapshotTargetMismatchBeforeOwnerCall() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "CMD-201");
        assertThat(adapter.execute(command(risk, "requester-01")).status())
                .isEqualTo(AdmApprovalExecutionStatus.FAILED);

        AdmApprovedOperationCommand valid = command(risk, "approver-02");
        AdmApprovedOperationCommand mismatch = new AdmApprovedOperationCommand(
                valid.approvalRequestId(), valid.commandRequestId(), valid.actionType(),
                valid.ownerModule(), valid.ownerCommand(), valid.targetType(), "43",
                valid.payloadHash(), valid.payloadSnapshot(), valid.requestedBy(),
                valid.approvedBy(), valid.reason(), valid.transactionId());
        assertThat(adapter.execute(mismatch).status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        verify(batch, never()).requestRetry(anyLong(), any(CpfBatchRiskCommand.class));
    }

    @Test
    void mapsOwnerUnknownUsingTheOwnerFailureCode() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "CMD-201");
        when(batch.requestRetry(eq(42L), any(CpfBatchRiskCommand.class)))
                .thenThrow(new CpfBatchOwnerUnknownResultException("BAT-UNKNOWN", "timeout"));

        var result = adapter.execute(command(risk, "approver-02"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
        assertThat(result.resultCode()).isEqualTo("BAT-UNKNOWN");
    }

    private AdmApprovedOperationCommand command(CpfBatchRiskCommand risk, String approvedBy)
            throws Exception {
        return new AdmApprovedOperationCommand(
                201L, risk.idempotencyKey(), risk.actionType(), "BAT", risk.operation(),
                risk.targetType(), risk.targetId(), risk.fingerprint(),
                objectMapper.writeValueAsString(risk), risk.requestUser(), approvedBy,
                "execution audit reason", "TX-201");
    }

    private static CpfBatchRiskCommand risk(String requester, String idempotencyKey) {
        return new CpfBatchRiskCommand(
                "requestRetry", "bat_execution", "42", "BATCH_RETRY", requester,
                "incident recovery", "201", idempotencyKey, 7L, "");
    }
}
