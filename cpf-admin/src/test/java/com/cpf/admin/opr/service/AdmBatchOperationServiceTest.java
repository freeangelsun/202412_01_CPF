package com.cpf.admin.opr.service;

import com.cpf.batch.api.CpfBatchOperationsPort;
import com.cpf.batch.api.CpfBatchRiskCommand;
import com.cpf.data.api.CpfDataRow;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ADM Batch Control Plane은 BAT Runtime 구현이나 batDB를 직접 소유하지 않고
 * {@link CpfBatchOperationsPort} 계약에만 위임해야 함을 검증합니다.
 */
class AdmBatchOperationServiceTest {

    private final CpfBatchOperationsPort operations = mock(CpfBatchOperationsPort.class);
    private final AdmBatchApprovalService approvals = mock(AdmBatchApprovalService.class);
    private final AdmBatchOperationService service = new AdmBatchOperationService(operations, approvals);

    @Test
    void findWorkersDelegatesToBatOwnerPort() {
        List<CpfDataRow> expected = List.of(CpfDataRow.of("workerId", "bat-worker-01"));
        when(operations.findWorkers(30)).thenReturn(expected);

        assertThat(service.findWorkers(30)).isSameAs(expected);
        verify(operations).findWorkers(30);
    }

    @Test
    void findGhostCandidatesDelegatesToBatOwnerPort() {
        List<CpfDataRow> expected = List.of(CpfDataRow.of("executionId", 10L));
        when(operations.findGhostCandidates(30)).thenReturn(expected);

        assertThat(service.findGhostCandidates(30)).isSameAs(expected);
        verify(operations).findGhostCandidates(30);
    }

    @Test
    void releaseLockDelegatesDangerousOperationWithAuditContext() {
        String lockKey = "batch:job:CPF_EDU_TASKLET_JOB:test";
        CpfBatchRiskCommand command = command("releaseLock", "bat_lock", lockKey, "BATCH_LOCK_RELEASE");
        AdmBatchApprovalService.Reservation reservation = reservation();
        CpfDataRow expected = CpfDataRow.of("lockKey", lockKey, "released", true);
        when(approvals.reserve(command)).thenReturn(reservation);
        when(operations.releaseLock(eq(lockKey), any(CpfBatchRiskCommand.class))).thenReturn(expected);

        assertThat(service.releaseLock(lockKey, command)).isSameAs(expected);
        verify(operations).releaseLock(eq(lockKey), any(CpfBatchRiskCommand.class));
        verify(approvals).complete(reservation, "approver-01");
    }

    @Test
    void actGhostDelegatesToBatOwnerPort() {
        CpfBatchRiskCommand command = command(
                "actGhostExecution", "bat_execution", "10", "BATCH_GHOST_FAIL");
        AdmBatchApprovalService.Reservation reservation = reservation();
        CpfDataRow expected = CpfDataRow.of("actionType", "FAIL", "executionId", 10L);
        when(approvals.reserve(command)).thenReturn(reservation);
        when(operations.actGhostExecution(eq(10L), eq("FAIL"), any(CpfBatchRiskCommand.class)))
                .thenReturn(expected);

        assertThat(service.actGhostExecution(10L, "FAIL", command)).isSameAs(expected);
        verify(operations).actGhostExecution(eq(10L), eq("FAIL"), any(CpfBatchRiskCommand.class));
        verify(approvals).complete(reservation, "approver-01");
    }

    @Test
    void requestRunDelegatesToBatOwnerPort() {
        String jobId = "CPF_EDU_TASKLET_JOB";
        CpfBatchRiskCommand command = command("requestRun", "bat_job", jobId, "BATCH_RUN");
        AdmBatchApprovalService.Reservation reservation = reservation();
        CpfDataRow expected = CpfDataRow.of("jobId", jobId, "accepted", true);
        when(approvals.reserve(command)).thenReturn(reservation);
        when(operations.requestRun(eq(jobId), eq("businessDate=20260726"), any(CpfBatchRiskCommand.class)))
                .thenReturn(expected);

        assertThat(service.requestRun(jobId, "businessDate=20260726", command)).isSameAs(expected);
        verify(operations).requestRun(eq(jobId), eq("businessDate=20260726"), any(CpfBatchRiskCommand.class));
        verify(approvals).complete(reservation, "approver-01");
    }

    private static CpfBatchRiskCommand command(
            String operation, String targetType, String targetId, String actionType) {
        return new CpfBatchRiskCommand(
                operation, targetType, targetId, actionType,
                "approver-01", "운영 복구", "41", "idem-41", 1L, "");
    }

    private static AdmBatchApprovalService.Reservation reservation() {
        return new AdmBatchApprovalService.Reservation(41L, "idem-41", "RUNNING", false, "maker-01");
    }
}
