package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.opr.dto.AdmCacheControlResponse;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmCacheOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CacheApprovalOwnerCommandAdapterTest {
    private final AdmCacheOperationService service = mock(AdmCacheOperationService.class);
    private final AdmAuditLogService audit = mock(AdmAuditLogService.class);
    private final CacheApprovalOwnerCommandAdapter adapter = new CacheApprovalOwnerCommandAdapter(service, audit, new ObjectMapper());

    @Test
    void approvedKeyEvictionUsesImmutableSnapshotAndIndependentApprover() {
        when(service.evictKey("TENANT", "users", "42", 7L, "approver", "approved reason"))
                .thenReturn(new AdmCacheControlResponse("EVICT_KEY", "users:42", true, 1, null, 0, Instant.now(), "ok"));
        var result = adapter.execute(command(CacheApprovalOwnerCommandAdapter.EVICT_KEY, "TENANT:users:42",
                "{\"tenantId\":\"TENANT\",\"namespace\":\"users\",\"key\":\"42\",\"version\":7}"));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        verify(service).evictKey("TENANT", "users", "42", 7L, "approver", "approved reason");
        verify(audit).record("20260815110000000ADM00000010000001", "approver", CacheApprovalOwnerCommandAdapter.EVICT_KEY,
                "cache", "TENANT:users:42", "approved reason", "approval-engine");
    }

    @Test
    void selfApprovalFailsClosedWithoutMutation() {
        var command = new AdmApprovedOperationCommand(1, "cmd", CacheApprovalOwnerCommandAdapter.RECONCILE,
                CacheApprovalOwnerCommandAdapter.OWNER_MODULE, CacheApprovalOwnerCommandAdapter.RECONCILE,
                CacheApprovalOwnerCommandAdapter.TARGET_TYPE, "DURABLE", "0".repeat(64), "{}",
                "same", "same", "approved reason", "20260815110000000ADM00000010000001", "lease", 1);
        assertThat(adapter.execute(command).status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        verifyNoInteractions(service);
    }

    @Test
    void unknownEvictionReconcileNeverReplaysMutation() {
        var result = adapter.reconcile(command(CacheApprovalOwnerCommandAdapter.EVICT_NAMESPACE, "TENANT:users",
                "{\"tenantId\":\"TENANT\",\"namespace\":\"users\",\"version\":7}"));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.UNKNOWN);
        verifyNoInteractions(service);
    }

    private static AdmApprovedOperationCommand command(String operation, String targetId, String payload) {
        return new AdmApprovedOperationCommand(1, "cmd", operation, CacheApprovalOwnerCommandAdapter.OWNER_MODULE,
                operation, CacheApprovalOwnerCommandAdapter.TARGET_TYPE, targetId, "0".repeat(64), payload,
                "requester", "approver", "approved reason", "20260815110000000ADM00000010000001", "lease", 1);
    }
}
