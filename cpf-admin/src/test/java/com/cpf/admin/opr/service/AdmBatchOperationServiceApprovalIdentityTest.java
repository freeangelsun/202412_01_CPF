package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.data.api.CpfDataRow;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdmBatchOperationServiceApprovalIdentityTest {
    @Test
    void remoteOwnerReceivesRequesterWhileApprovalCompletionUsesExecutor() {
        CpfBatchOperationsPort owner = mock(CpfBatchOperationsPort.class);
        AdmBatchApprovalService approvals = mock(AdmBatchApprovalService.class);
        AdmBatchOperationService service = new AdmBatchOperationService(owner, approvals);
        CpfBatchRiskCommand executorCommand = new CpfBatchRiskCommand(
                "requestRetry", "bat_execution", "42", "BATCH_RETRY",
                "approver-02", "incident recovery", "101", "idem-101", 7L, "");
        AdmBatchApprovalService.Reservation reservation =
                new AdmBatchApprovalService.Reservation(101L, "idem-101", "RUNNING", false, "requester-01");
        CpfDataRow response = mock(CpfDataRow.class);
        when(approvals.reserve(executorCommand)).thenReturn(reservation);
        when(owner.requestRetry(org.mockito.ArgumentMatchers.eq(42L), any(CpfBatchRiskCommand.class)))
                .thenReturn(response);

        assertThat(service.requestRetry(42L, executorCommand)).isSameAs(response);

        ArgumentCaptor<CpfBatchRiskCommand> ownerCommand =
                ArgumentCaptor.forClass(CpfBatchRiskCommand.class);
        verify(owner).requestRetry(org.mockito.ArgumentMatchers.eq(42L), ownerCommand.capture());
        assertThat(ownerCommand.getValue().requestUser()).isEqualTo("requester-01");
        verify(approvals).complete(reservation, "approver-02");
    }
}
