package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeControlPlane;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RuntimeControlApprovalOwnerCommandAdapterTest {

    @Test
    void reconcileQueriesTheExactApprovedPayloadCommandIdWithoutReplayingMutation() {
        CpfRuntimeControlPlane control = mock(CpfRuntimeControlPlane.class);
        CpfRuntimeChangeResult terminal = new CpfRuntimeChangeResult(
                "change-17", "runtime-command-17", "CONFIG", "SUCCESS", 3L, "hash",
                1, 1, 0, 0, null, null, Instant.now(), Instant.now(), "applied");
        when(control.getByCommandId("runtime-command-17")).thenReturn(terminal);
        var adapter = new RuntimeControlApprovalOwnerCommandAdapter(control, new ObjectMapper());

        var result = adapter.reconcile(command("{\"commandId\":\"runtime-command-17\"}"));

        assertThat(result.status()).isEqualTo(AdmApprovalExecutionStatus.SUCCEEDED);
        assertThat(result.resultCode()).isEqualTo("RUNTIME_RECONCILE_SUCCESS");
        verify(control).getByCommandId("runtime-command-17");
    }

    private static AdmApprovedOperationCommand command(String payload) {
        return new AdmApprovedOperationCommand(
                17L,
                "approval-command-17",
                RuntimeControlApprovalOwnerCommandAdapter.ACTION,
                RuntimeControlApprovalOwnerCommandAdapter.OWNER_MODULE,
                RuntimeControlApprovalOwnerCommandAdapter.CREATE,
                RuntimeControlApprovalOwnerCommandAdapter.TARGET_TYPE,
                "change-17",
                "0".repeat(64),
                payload,
                "requester-a",
                "approver-b",
                "approved runtime change",
                "20260822180000000ADM00000010000001",
                "lease-17",
                3L);
    }
}
