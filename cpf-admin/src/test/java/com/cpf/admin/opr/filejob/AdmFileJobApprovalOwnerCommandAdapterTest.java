package com.cpf.admin.opr.filejob;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdmFileJobApprovalOwnerCommandAdapterTest {
    private static final String HASH="a".repeat(64);
    private final AdmFileJobService service=mock(AdmFileJobService.class);
    private final AdmFileJobApprovalOwnerCommandAdapter adapter=new AdmFileJobApprovalOwnerCommandAdapter(service,new ObjectMapper());

    @Test void exactOwnerTupleOnly(){
        assertEquals(true,adapter.supports("ADM","FILE_JOB_APPLY","FILE_JOB_APPLY","FILE_JOB"));
        assertEquals(false,adapter.supports("ADM","FILE_JOB_APPLY","FILE_JOB_RETRY","FILE_JOB"));
    }

    @Test void selfApprovalFailsBeforeOwnerRead(){
        var result=adapter.execute(command("FILE_JOB_APPLY","job-1","{\"expectedState\":\"VALIDATED\"}","same","same"));
        assertEquals(AdmApprovalExecutionStatus.FAILED,result.status());
        verifyNoInteractions(service);
    }

    @Test void changedStateFailsClosedBeforeMutation(){
        when(service.get("job-1")).thenReturn(job("job-1",AdmFileJobState.FAILED,null));
        var result=adapter.execute(command("FILE_JOB_APPLY","job-1","{\"expectedState\":\"VALIDATED\"}","requester","approver"));
        assertEquals(AdmApprovalExecutionStatus.FAILED,result.status());
        verify(service,never()).apply(anyString(),anyString(),anyString(),anyString());
    }

    @Test void approvedApplyUsesServerApprovalIdAndRequiresReconcileWhileRunning(){
        when(service.get("job-1")).thenReturn(job("job-1",AdmFileJobState.VALIDATED,null));
        when(service.apply("job-1","approver","approved reason","101"))
                .thenReturn(job("job-1",AdmFileJobState.APPLYING,"101"));
        var result=adapter.execute(command("FILE_JOB_APPLY","job-1","{\"expectedState\":\"VALIDATED\"}","requester","approver"));
        assertEquals(AdmApprovalExecutionStatus.RUNNING,result.status());
        verify(service).apply("job-1","approver","approved reason","101");
    }

    @Test void reconcileNeverReplaysMutationAndRequiresSameApprovalIdentity(){
        var command=command("FILE_JOB_APPLY","job-1","{\"expectedState\":\"VALIDATED\"}","requester","approver");
        when(service.get("job-1")).thenReturn(job("job-1",AdmFileJobState.COMPLETED,"101"));
        var result=adapter.reconcile(command);
        assertEquals(AdmApprovalExecutionStatus.SUCCEEDED,result.status());
        verify(service,never()).apply(anyString(),anyString(),anyString(),anyString());
    }

    private static AdmApprovedOperationCommand command(String action,String targetId,String payload,String requester,String approver){
        return new AdmApprovedOperationCommand(101L,"cmd-101",action,"ADM",action,"FILE_JOB",targetId,HASH,payload,
                requester,approver,"approved reason","20260815000000000ADM00000010000001","lease",1L);
    }
    private static AdmFileJobResponse job(String id,AdmFileJobState state,String approvalId){
        return new AdmFileJobResponse(id,"op-1","hash",AdmFileJobType.UPLOAD,"TEMPLATE",1,"CSV",state,false,true,
                1,1,0,"src","result","requester","reason",approvalId,"approver",null,"approver","approved reason",Instant.now(),
                null,null,Instant.now().plusSeconds(3600),Instant.now(),Instant.now());
    }
}
