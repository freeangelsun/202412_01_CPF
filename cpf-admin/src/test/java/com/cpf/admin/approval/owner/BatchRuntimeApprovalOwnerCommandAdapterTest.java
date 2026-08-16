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
import com.cpf.admin.opr.batch.runtime.BatchRuntimeControlClient;
import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.batch.api.CpfBatchOwnerUnknownResultException;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.data.api.CpfDataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
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


    @Test
    void ownerTupleMatchingIsExactAndRejectsNearMatches() {
        assertThat(adapter.supports("BAT", "requestRetry", "BATCH_RETRY", "bat_execution")).isTrue();
        assertThat(adapter.supports("bat", "requestRetry", "BATCH_RETRY", "bat_execution")).isFalse();
        assertThat(adapter.supports("BAT", "requestRetry", "batch_retry", "bat_execution")).isFalse();
        assertThat(adapter.supports("BAT", "requestRetry", "BATCH_RETRY", "BAT_EXECUTION")).isFalse();
        assertThat(adapter.supports("batch-runtime", "requestRetry", "BATCH_RETRY", "bat_execution")).isFalse();
        assertThat(adapter.supports("BAT", "requestRetry", "BATCH_RETRY_FORCE", "bat_execution")).isFalse();
        assertThat(adapter.supports("BAT", "requestRetry", "OPS_RETRY", "bat_execution")).isFalse();
        assertThat(adapter.supports("BAT", "requestRetry", "BATCH_RETRY", "bat_execution_shadow")).isFalse();
        assertThat(adapter.supports("BAT", "requestRun", "BATCH_SCHEDULER_RUN_ONCE", "bat_job")).isFalse();
    }

    @Test
    void executesApprovedSchedulerUnknownReconcileThroughBatOwnerClient() throws Exception {
        BatchRuntimeControlClient runtimeClient = mock(BatchRuntimeControlClient.class);
        BatchRuntimeApprovalOwnerCommandAdapter schedulerAdapter =
                new BatchRuntimeApprovalOwnerCommandAdapter(batch, objectMapper, runtimeClient);
        CpfBatchRiskCommand risk = schedulerRisk("requester-01", "SCH-RCN-201");
        when(runtimeClient.schedulerTriggerReconcileApproved(any(), any(), any(), any()))
                .thenReturn(CpfDataRow.of("status", "FAILED"));

        var result = schedulerAdapter.execute(command(risk, "approver-02"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> request = ArgumentCaptor.forClass(Map.class);
        verify(runtimeClient).schedulerTriggerReconcileApproved(
                eq("SCH-01"), request.capture(), eq("201"), eq("requester-01"));
        assertThat(request.getValue()).containsEntry("scheduledFireAt", "2026-08-15T01:00:00Z");
        assertThat(request.getValue()).containsEntry("expectedTriggerIdempotencyKey", "TRIGGER-001");
        assertThat(request.getValue()).containsEntry("expectedAttemptCount", 2L);
        assertThat(request.getValue()).containsEntry("idempotencyKey", "SCH-RCN-201");
        assertThat(request.getValue()).containsEntry("requestedBy", "requester-01");
        assertThat(request.getValue()).containsEntry("approvedBy", "approver-02");
        assertThat(request.getValue()).containsEntry("approvalRequestId", "201");
    }

    @Test
    void reconcilesSchedulerUnknownOnlyWhenOwnerIdentityAndStateConverge() throws Exception {
        BatchRuntimeControlClient runtimeClient = mock(BatchRuntimeControlClient.class);
        BatchRuntimeApprovalOwnerCommandAdapter schedulerAdapter =
                new BatchRuntimeApprovalOwnerCommandAdapter(batch, objectMapper, runtimeClient);
        CpfBatchRiskCommand risk = schedulerRisk("requester-01", "SCH-RCN-201");
        AdmApprovedOperationCommand command = command(risk, "approver-02");
        when(runtimeClient.schedulerTriggerState("SCH-01", "2026-08-15T01:00:00Z"))
                .thenReturn(CpfDataRow.of(
                        "idempotencyKey", "TRIGGER-001",
                        "triggerStatus", "FAILED",
                        "attemptCount", 2));

        var converged = schedulerAdapter.reconcile(command);
        assertThat(converged.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);

        when(runtimeClient.schedulerTriggerState("SCH-01", "2026-08-15T01:00:00Z"))
                .thenReturn(CpfDataRow.of(
                        "idempotencyKey", "TRIGGER-001",
                        "triggerStatus", "UNKNOWN",
                        "attemptCount", 2));
        assertThat(schedulerAdapter.reconcile(command).status())
                .isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);

        when(runtimeClient.schedulerTriggerState("SCH-01", "2026-08-15T01:00:00Z"))
                .thenReturn(CpfDataRow.of(
                        "idempotencyKey", "OTHER-TRIGGER",
                        "triggerStatus", "FAILED",
                        "attemptCount", 2));
        assertThat(schedulerAdapter.reconcile(command).status())
                .isEqualTo(AdmApprovalExecutionStatus.FAILED);
    }

    private AdmApprovedOperationCommand command(CpfBatchRiskCommand risk, String approvedBy)
            throws Exception {
        return new AdmApprovedOperationCommand(
                201L, risk.idempotencyKey(), risk.actionType(), "BAT", risk.operation(),
                risk.targetType(), risk.targetId(), risk.fingerprint(),
                objectMapper.writeValueAsString(risk), risk.requestUser(), approvedBy,
                "execution audit reason", "TX-201");
    }

    private static CpfBatchRiskCommand schedulerRisk(String requester, String idempotencyKey) {
        return new CpfBatchRiskCommand(
                "reconcileSchedulerTrigger", "bat_schedule_trigger", "SCH-01",
                "BATCH_SCHEDULER_RECONCILE_UNKNOWN", requester,
                "scheduler unknown recovery", "201", idempotencyKey, null,
                "{\"scheduledFireAt\":\"2026-08-15T01:00:00Z\","
                        + "\"expectedTriggerIdempotencyKey\":\"TRIGGER-001\","
                        + "\"expectedAttemptCount\":2}");
    }

    private static CpfBatchRiskCommand risk(String requester, String idempotencyKey) {
        return new CpfBatchRiskCommand(
                "requestRetry", "bat_execution", "42", "BATCH_RETRY", requester,
                "incident recovery", "201", idempotencyKey, 7L, "");
    }
    @Test
    void reconcileRequiresExactStructuredOperationIdentity() throws Exception {
        CpfBatchRiskCommand risk = risk("requester-01", "CMD-201");
        AdmApprovedOperationCommand command = command(risk, "approver-02");
        CpfDataRow collision = CpfDataRow.of(
                "commandRequestId", command.commandRequestId() + "-shadow",
                "idempotencyKey", risk.idempotencyKey(),
                "approvalRequestId", String.valueOf(command.approvalRequestId()),
                "operation", risk.operation(),
                "targetType", risk.targetType(),
                "targetId", risk.targetId(),
                "status", "SUCCEEDED");
        when(batch.findOperationLogs(null, 42L, 1000)).thenReturn(List.of(collision));

        assertThat(adapter.reconcile(command).status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
    }

}
