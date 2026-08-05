package com.cpf.admin.opr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import com.cpf.core.api.error.CpfValidationException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AdmCenterCutOperationServiceTest {
    @Test
    void reprocessExecutesOnlyMatchingCanonicalApprovalRequest() {
        CpfCenterCutOperationsPort port = mock(CpfCenterCutOperationsPort.class);
        AdmApprovalService approvals = mock(AdmApprovalService.class);
        AdmCenterCutOperationService service = new AdmCenterCutOperationService(port, approvals);
        when(approvals.detail(201L)).thenReturn(approval(
                "idem-201", "reprocessCenterCutFailed", "EX-9", "CENTER_CUT_REPROCESS_FAILED"));
        when(approvals.execute(201L, "incident recovery", "approver-02"))
                .thenReturn(Map.of("approvalRequestId", 201L, "status", "COMPLETED"));

        Map<String, Object> result = service.reprocessFailed(
                "EX-9", 201L, "idem-201", "incident recovery", "approver-02");

        assertThat(result).containsEntry("status", "COMPLETED");
        verify(approvals).detail(201L);
        verify(approvals).execute(201L, "incident recovery", "approver-02");
    }

    @Test
    void reconcileExecutesOnlyMatchingUnknownAction() {
        CpfCenterCutOperationsPort port = mock(CpfCenterCutOperationsPort.class);
        AdmApprovalService approvals = mock(AdmApprovalService.class);
        AdmCenterCutOperationService service = new AdmCenterCutOperationService(port, approvals);
        when(approvals.detail(202L)).thenReturn(approval(
                "idem-202", "reconcileCenterCutUnknown", "EX-10", "CENTER_CUT_RECONCILE_UNKNOWN"));
        when(approvals.execute(202L, "unknown reconciliation", "approver-03"))
                .thenReturn(Map.of("approvalRequestId", 202L, "status", "UNKNOWN"));

        Map<String, Object> result = service.reconcileUnknown(
                "EX-10", 202L, "idem-202", "unknown reconciliation", "approver-03");

        assertThat(result).containsEntry("status", "UNKNOWN");
        verify(approvals).execute(202L, "unknown reconciliation", "approver-03");
    }

    @Test
    void divergentIdempotencyOrTargetFailsBeforeOwnerExecution() {
        CpfCenterCutOperationsPort port = mock(CpfCenterCutOperationsPort.class);
        AdmApprovalService approvals = mock(AdmApprovalService.class);
        AdmCenterCutOperationService service = new AdmCenterCutOperationService(port, approvals);
        when(approvals.detail(201L)).thenReturn(approval(
                "different-key", "reprocessCenterCutFailed", "EX-OTHER", "CENTER_CUT_REPROCESS_FAILED"));

        assertThatThrownBy(() -> service.reprocessFailed(
                "EX-9", 201L, "idem-201", "incident recovery", "approver-02"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("requestKey");
    }

    @Test
    void invalidApprovalIdAndBlankOperationalFieldsFailClosed() {
        CpfCenterCutOperationsPort port = mock(CpfCenterCutOperationsPort.class);
        AdmApprovalService approvals = mock(AdmApprovalService.class);
        AdmCenterCutOperationService service = new AdmCenterCutOperationService(port, approvals);

        assertThatThrownBy(() -> service.reprocessFailed(
                "EX-9", 0L, "idem", "reason", "operator"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("approvalRequestId");
        assertThatThrownBy(() -> service.reprocessFailed(
                "EX-9", 201L, " ", "reason", "operator"))
                .isInstanceOf(CpfValidationException.class)
                .hasMessageContaining("idempotencyKey");
    }

    private static Map<String, Object> approval(
            String requestKey, String ownerCommand, String targetId, String actionType) {
        return Map.of(
                "requestKey", requestKey,
                "ownerModule", "BAT",
                "ownerCommand", ownerCommand,
                "targetType", "center_cut_execution",
                "targetId", targetId,
                "actionType", actionType);
    }
}
