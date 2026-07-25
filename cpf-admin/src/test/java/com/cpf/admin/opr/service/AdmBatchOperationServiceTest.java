package com.cpf.admin.opr.service;

import com.cpf.core.api.batch.CpfBatchOperationsPort;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ADM Batch Control Plane은 BAT Runtime 구현이나 batDB를 직접 소유하지 않고
 * {@link CpfBatchOperationsPort} 계약에만 위임해야 함을 검증합니다.
 */
class AdmBatchOperationServiceTest {

    private final CpfBatchOperationsPort operations = mock(CpfBatchOperationsPort.class);
    private final AdmBatchOperationService service = new AdmBatchOperationService(operations);

    @Test
    void findWorkersDelegatesToBatOwnerPort() {
        List<Map<String, Object>> expected = List.of(Map.of("workerId", "bat-worker-01"));
        when(operations.findWorkers(30)).thenReturn(expected);

        assertThat(service.findWorkers(30)).isSameAs(expected);
        verify(operations).findWorkers(30);
    }

    @Test
    void findGhostCandidatesDelegatesToBatOwnerPort() {
        List<Map<String, Object>> expected = List.of(Map.of("executionId", 10L));
        when(operations.findGhostCandidates(30)).thenReturn(expected);

        assertThat(service.findGhostCandidates(30)).isSameAs(expected);
        verify(operations).findGhostCandidates(30);
    }

    @Test
    void releaseLockDelegatesDangerousOperationWithAuditContext() {
        Map<String, Object> expected = new LinkedHashMap<>();
        expected.put("lockKey", "batch:job:CPF_EDU_TASKLET_JOB:test");
        expected.put("released", true);
        when(operations.releaseLock(
                "batch:job:CPF_EDU_TASKLET_JOB:test",
                "adm-operator",
                "장애 복구 lock 해제"))
                .thenReturn(expected);

        assertThat(service.releaseLock(
                "batch:job:CPF_EDU_TASKLET_JOB:test",
                "adm-operator",
                "장애 복구 lock 해제"))
                .isSameAs(expected);
        verify(operations).releaseLock(
                "batch:job:CPF_EDU_TASKLET_JOB:test",
                "adm-operator",
                "장애 복구 lock 해제");
    }

    @Test
    void actGhostDelegatesToBatOwnerPort() {
        Map<String, Object> expected = Map.of("actionType", "FAIL", "executionId", 10L);
        when(operations.actGhostExecution(
                10L,
                "FAIL",
                "adm-operator",
                "heartbeat 장기 미수신으로 실패 처리"))
                .thenReturn(expected);

        assertThat(service.actGhostExecution(
                10L,
                "FAIL",
                "adm-operator",
                "heartbeat 장기 미수신으로 실패 처리"))
                .isSameAs(expected);
        verify(operations).actGhostExecution(
                10L,
                "FAIL",
                "adm-operator",
                "heartbeat 장기 미수신으로 실패 처리");
    }

    @Test
    void requestRunDelegatesToBatOwnerPort() {
        Map<String, Object> expected = Map.of("jobId", "CPF_EDU_TASKLET_JOB", "accepted", true);
        when(operations.requestRun(
                "CPF_EDU_TASKLET_JOB",
                "businessDate=20260726",
                "adm-operator",
                "운영 재실행"))
                .thenReturn(expected);

        assertThat(service.requestRun(
                "CPF_EDU_TASKLET_JOB",
                "businessDate=20260726",
                "adm-operator",
                "운영 재실행"))
                .isSameAs(expected);
        verify(operations).requestRun(
                "CPF_EDU_TASKLET_JOB",
                "businessDate=20260726",
                "adm-operator",
                "운영 재실행");
    }
}
