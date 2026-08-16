package com.cpf.admin.approval.owner;

import com.cpf.admin.approval.api.*;
import com.cpf.platform.operations.api.featureflag.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class FeatureFlagApprovalOwnerCommandAdapterTest {
    private static final String HASH="a".repeat(64);
    private final CpfFeatureFlagOperations operations=mock(CpfFeatureFlagOperations.class);
    private final FeatureFlagApprovalOwnerCommandAdapter adapter=new FeatureFlagApprovalOwnerCommandAdapter(operations,new ObjectMapper());
    @Test void selfApprovalFailsClosed(){var r=adapter.execute(command("same","same"));assertEquals(AdmApprovalExecutionStatus.FAILED,r.status());verifyNoInteractions(operations);}
    @Test void revisionConflictBlocksMutation(){when(operations.find("flag.a")).thenReturn(result(CpfFeatureFlagResult.Source.PROVIDER,8));var r=adapter.execute(command("req","app"));assertEquals("FEATURE_FLAG_VERSION_CONFLICT",r.resultCode());verify(operations,never()).setKillSwitch(anyString(),anyBoolean(),anyString(),anyString());}
    @Test void approvedChangeConvergesToKillSwitch(){when(operations.find("flag.a")).thenReturn(result(CpfFeatureFlagResult.Source.PROVIDER,7),result(CpfFeatureFlagResult.Source.KILL_SWITCH,8));var r=adapter.execute(command("req","app"));assertEquals(AdmApprovalExecutionStatus.SUCCEEDED,r.status());verify(operations).setKillSwitch("flag.a",true,"app","approved reason");}
    @Test void reconcileOnlyObserves(){when(operations.find("flag.a")).thenReturn(result(CpfFeatureFlagResult.Source.KILL_SWITCH,8));var r=adapter.reconcile(command("req","app"));assertEquals(AdmApprovalExecutionStatus.SUCCEEDED,r.status());verify(operations,never()).setKillSwitch(anyString(),anyBoolean(),anyString(),anyString());}
    private static AdmApprovedOperationCommand command(String requester,String approver){return new AdmApprovedOperationCommand(101L,"cmd","FEATURE_FLAG_KILL_SWITCH","CPF-PLATFORM-OPERATIONS","FEATURE_FLAG_KILL_SWITCH","FEATURE_FLAG","flag.a",HASH,"{\"enabled\":true,\"expectedRevision\":7}",requester,approver,"approved reason","20260815000000000ADM00000010000001","lease",1);}
    private static CpfFeatureFlagResult<CpfFeatureFlagValue> result(CpfFeatureFlagResult.Source source,long revision){return new CpfFeatureFlagResult<>("flag.a",new CpfFeatureFlagValue.BooleanValue(false),null,"TEST",source,revision,Instant.now());}
}
