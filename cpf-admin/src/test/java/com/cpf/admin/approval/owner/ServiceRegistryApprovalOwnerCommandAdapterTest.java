package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.opr.service.AdmControlPlaneService;
import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServiceRegistryApprovalOwnerCommandAdapterTest {
    private static final String HASH = "a".repeat(64);

    private final CpfServiceRegistryQueryPort queryPort = mock(CpfServiceRegistryQueryPort.class);
    private final AdmControlPlaneService controlPlane = mock(AdmControlPlaneService.class);
    private final ServiceRegistryApprovalOwnerCommandAdapter adapter =
            new ServiceRegistryApprovalOwnerCommandAdapter(queryPort, controlPlane);

    @Test
    void supportsOnlyExactDeleteOwnerCommandActionTargetTuple() {
        assertEquals(true, adapter.supports(
                "CPF-INTEGRATION", "SERVICE_REGISTRY_SERVICE_DELETE",
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE"));
        assertEquals(false, adapter.supports(
                "CPF-INTEGRATION", "SERVICE_REGISTRY_SERVICE_DELETE",
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_ENDPOINT"));
        assertEquals(false, adapter.supports(
                "CPF-INTEGRATION-SHADOW", "SERVICE_REGISTRY_SERVICE_DELETE",
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE"));
    }

    @Test
    void selfApprovalFailsClosedBeforeOwnerMutation() {
        var result = adapter.execute(command(
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE", "MBR@3", "same-user", "same-user"));

        assertEquals(AdmApprovalExecutionStatus.FAILED, result.status());
        assertEquals("SERVICE_REGISTRY_SELF_APPROVAL", result.resultCode());
        verify(controlPlane, never()).executeApprovedRegistryDelete(
                eq("cmd-101"), eq("SERVICE_REGISTRY_SERVICE"), eq("MBR"), anyLong(), eq("approved delete"), eq("same-user"));
    }

    @Test
    void versionConflictFailsBeforeOwnerMutation() {
        when(queryPort.services("MBR", null, 10)).thenReturn(List.of(service("MBR", 4L)));

        var result = adapter.execute(command(
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE", "MBR@3", "requester", "approver"));

        assertEquals(AdmApprovalExecutionStatus.FAILED, result.status());
        assertEquals("SERVICE_REGISTRY_DELETE_VERSION_CONFLICT", result.resultCode());
        verify(controlPlane, never()).executeApprovedRegistryDelete(
                eq("cmd-101"), eq("SERVICE_REGISTRY_SERVICE"), eq("MBR"), anyLong(), eq("approved delete"), eq("approver"));
    }

    @Test
    void approvedDeleteExecutesOwnerExactlyOnceWithSnapshotVersion() {
        when(queryPort.services("MBR", null, 10)).thenReturn(List.of(service("MBR", 3L)));
        when(controlPlane.executeApprovedRegistryDelete(
                "cmd-101", "SERVICE_REGISTRY_SERVICE", "MBR", 3L, "approved delete", "approver"))
                .thenReturn(new CpfServiceRegistryView.MutationResult(
                        "SERVICE", "MBR", "CMD-101", "DELETED", 4L, OffsetDateTime.now()));

        var result = adapter.execute(command(
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE", "MBR@3", "requester", "approver"));

        assertEquals(AdmApprovalExecutionStatus.SUCCEEDED, result.status());
        verify(controlPlane).executeApprovedRegistryDelete(
                "cmd-101", "SERVICE_REGISTRY_SERVICE", "MBR", 3L, "approved delete", "approver");
    }

    @Test
    void deleteReconcileDoesNotReplayMutationAndConvergesFromOwnerState() {
        var command = command(
                "SERVICE_REGISTRY_SERVICE_DELETE", "SERVICE_REGISTRY_SERVICE", "MBR@3", "requester", "approver");
        when(queryPort.services("MBR", null, 10)).thenReturn(List.of(), List.of(service("MBR", 3L)));

        var absent = adapter.reconcile(command);
        var stillPresent = adapter.reconcile(command);

        assertEquals(AdmApprovalExecutionStatus.SUCCEEDED, absent.status());
        assertEquals("SERVICE_REGISTRY_DELETE_RECONCILED", absent.resultCode());
        assertEquals(AdmApprovalExecutionStatus.UNKNOWN, stillPresent.status());
        assertEquals("SERVICE_REGISTRY_DELETE_RECONCILE_PENDING", stillPresent.resultCode());
        verify(controlPlane, never()).executeApprovedRegistryDelete(
                eq("cmd-101"), eq("SERVICE_REGISTRY_SERVICE"), eq("MBR"), anyLong(), eq("approved delete"), eq("approver"));
    }

    private static AdmApprovedOperationCommand command(
            String action, String targetType, String targetId, String requester, String approver) {
        return new AdmApprovedOperationCommand(
                101L, "cmd-101", action, "CPF-INTEGRATION", action, targetType, targetId,
                HASH, requester, approver, "approved delete", "20260815000000000ADM00000010000001");
    }

    private static CpfServiceRegistryView.Service service(String id, long version) {
        return new CpfServiceRegistryView.Service(
                id, "회원 서비스", "INTERNAL", "cpf-member", "member", true, version, OffsetDateTime.now());
    }
}
