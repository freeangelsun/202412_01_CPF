package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmDynamicLogLevelBroadcastService;
import com.cpf.admin.opr.service.AdmDynamicLogLevelRuleStore;
import com.cpf.platform.operations.observability.api.logging.CpfDynamicLogLevelOperations;
import com.cpf.platform.operations.observability.api.logging.CpfLogLevel;
import com.cpf.platform.operations.observability.api.logging.DynamicLogLevelRule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DynamicLogLevelApprovalOwnerCommandAdapterTest {
    private final CpfDynamicLogLevelOperations runtime = mock(CpfDynamicLogLevelOperations.class);
    private final AdmDynamicLogLevelRuleStore store = mock(AdmDynamicLogLevelRuleStore.class);
    private final AdmDynamicLogLevelBroadcastService broadcast = mock(AdmDynamicLogLevelBroadcastService.class);
    private final AdmAuditLogService audit = mock(AdmAuditLogService.class);
    private final DynamicLogLevelApprovalOwnerCommandAdapter adapter =
            new DynamicLogLevelApprovalOwnerCommandAdapter(runtime, store, broadcast, audit, new ObjectMapper());

    @Test
    void approvedRegisterUsesSnapshotAndIndependentApprover() {
        DynamicLogLevelRule rule = new DynamicLogLevelRule("RULE-1", "20260815110000000ADM00000010000001", null,
                "ADM", CpfLogLevel.DEBUG, "approved reason", "approver", LocalDateTime.now(), LocalDateTime.now().plusMinutes(10));
        when(runtime.register(any())).thenReturn(rule);
        var result = adapter.execute(command(DynamicLogLevelApprovalOwnerCommandAdapter.REGISTER, "20260815110000000ADM00000010000001",
                "{\"transactionId\":\"20260815110000000ADM00000010000001\",\"businessTransactionId\":null,\"logLevel\":\"DEBUG\",\"ttlSeconds\":600}"));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        verify(store).save(rule);
        verify(broadcast).publishUpsert(rule, "approver");
    }

    @Test
    void selfApprovalFailsClosed() {
        var command = new AdmApprovedOperationCommand(1, "cmd", DynamicLogLevelApprovalOwnerCommandAdapter.REMOVE,
                DynamicLogLevelApprovalOwnerCommandAdapter.OWNER_MODULE, DynamicLogLevelApprovalOwnerCommandAdapter.REMOVE,
                DynamicLogLevelApprovalOwnerCommandAdapter.TARGET_TYPE, "RULE-1", "0".repeat(64), "{\"ruleId\":\"RULE-1\"}",
                "same", "same", "approved reason", "20260815110000000ADM00000010000001", "lease", 1);
        assertThat(adapter.execute(command).status()).isEqualTo(AdmApprovalExecutionStatus.FAILED);
        verifyNoInteractions(runtime, store, broadcast, audit);
    }

    @Test
    void removeReconcileObservesStateWithoutReplay() {
        when(store.findActiveRules()).thenReturn(List.of());
        var result = adapter.reconcile(command(DynamicLogLevelApprovalOwnerCommandAdapter.REMOVE, "RULE-1", "{\"ruleId\":\"RULE-1\"}"));
        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        verify(store).findActiveRules();
        verifyNoInteractions(runtime, broadcast, audit);
    }

    private static AdmApprovedOperationCommand command(String operation, String targetId, String payload) {
        return new AdmApprovedOperationCommand(1, "cmd", operation, DynamicLogLevelApprovalOwnerCommandAdapter.OWNER_MODULE,
                operation, DynamicLogLevelApprovalOwnerCommandAdapter.TARGET_TYPE, targetId, "0".repeat(64), payload,
                "requester", "approver", "approved reason", "20260815110000000ADM00000010000001", "lease", 1);
    }
}
