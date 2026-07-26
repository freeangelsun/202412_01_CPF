package com.cpf.admin.opr.batch.runtime;

import com.cpf.admin.approval.api.*;
import com.cpf.admin.approval.spi.AdmApprovalOwnerCommandPort;
import com.cpf.batch.api.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.time.Instant;
import java.util.*;

@Component("BAT")
public class BatApprovalOwnerCommandPort implements AdmApprovalOwnerCommandPort {
 private final RestClient client;
 public BatApprovalOwnerCommandPort(RestClient.Builder builder,@Value("${cpf.batch.control.base-url:http://127.0.0.1:8180}")String baseUrl){client=builder.baseUrl(baseUrl).build();}
 @Override public AdmApprovedOperationResult execute(AdmApprovedOperationCommand c){
  if(!"BAT".equalsIgnoreCase(c.ownerModule()))return failed("BAT-OWNER-MISMATCH","BAT Owner mismatch");
  try{
   String owner=c.ownerCommand().toUpperCase(Locale.ROOT);
   if(Set.of("DEPLOY_PLAN","ROLLBACK_PLAN").contains(owner)){
    String endpoint="ROLLBACK_PLAN".equals(owner)?"/api/v1/batch/deployment-plans/{id}/rollback-approved":"/api/v1/batch/deployment-plans/{id}/execute-approved";
    ApprovedExecution body=new ApprovedExecution(c.approvalRequestId(),c.commandRequestId(),0L,"approval-request:"+c.approvalRequestId(),"ADM_APPROVAL_ENGINE","approved operation");
    DeploymentResult r=client.post().uri(endpoint,c.targetId()).body(body).retrieve().body(DeploymentResult.class);
    if(r==null)return unknown("BAT-NO-RESULT");
    return map(r.state(),"BAT-"+r.state(),r.message());
   }
   if(Set.of("START","STOP","RESTART","DRAIN","RESUME","ROLLBACK").contains(owner)){
    RuntimeCommand command=new RuntimeCommand(c.commandRequestId(),c.commandRequestId(),owner,c.targetType(),List.of(c.targetId()),
      c.targetId(),c.payloadHash(),0L,"approval-request:"+c.approvalRequestId(),"approved operation",Instant.now(),
      "ADM_APPROVAL",Long.toString(c.approvalRequestId()),"ADM_APPROVAL_ENGINE",Instant.now().plusSeconds(900),
      CommandState.APPROVED,0,Map.of(),null,null,null,null,c.transactionId(),null);
    Map<?,?> state=client.post().uri("/api/v1/batch/runtime/commands").body(command).retrieve().body(Map.class);
    if(state==null)return unknown("BAT-NO-RESULT");
    String s=Objects.toString(state.get("command_state"),Objects.toString(state.get("commandState"),"UNKNOWN_RESULT"));
    try{return map(CommandState.valueOf(s),"BAT-"+s,"BAT runtime command");}catch(IllegalArgumentException e){return unknown("BAT-UNKNOWN-STATE");}
   }
   return failed("BAT-UNSUPPORTED-COMMAND","Unsupported BAT owner command");
  }catch(RuntimeException e){return unknown("BAT-OWNER-EXCEPTION");}
 }
 private static AdmApprovedOperationResult map(CommandState s,String code,String msg){return switch(s){case SUCCEEDED,ROLLED_BACK->new AdmApprovedOperationResult(AdmApprovalExecutionStatus.SUCCEEDED,code,msg);case FAILED,PARTIALLY_ROLLED_BACK->failed(code,msg);default->unknown(code);};}
 private static AdmApprovedOperationResult failed(String c,String m){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.FAILED,c,m);}
 private static AdmApprovedOperationResult unknown(String c){return new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN,c,"Owner result is unknown; reconciliation required");}
 private record ApprovedExecution(long approvalRequestId,String commandRequestId,long expectedVersion,String requestedBy,String approvedBy,String reason){}
}
