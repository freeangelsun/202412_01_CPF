package com.cpf.admin.opr.batch.runtime;
import com.cpf.admin.approval.service.AdmApprovalService;
import java.util.*; import org.springframework.http.ResponseEntity;
public final class BatchRuntimeControllerHarness {
 static int assertions=0;
 static void ok(boolean v,String m){assertions++; if(!v)throw new AssertionError(m);}
 static void status(ResponseEntity<?> r,int s){ok(r.getStatusCode().value()==s,"status expected="+s+" actual="+r.getStatusCode().value());}
 static void noAlias(Object v,boolean top){if(v instanceof Map<?,?> m){for(var e:m.entrySet()){String k=String.valueOf(e.getKey());boolean canonical=top&&k.equals("requestedBy");ok(canonical||!Set.of("requestedBy","requestUser","actorId","operatorId","operatorIdOverride").contains(k),"actor alias leaked: "+k);noAlias(e.getValue(),false);}}else if(v instanceof List<?> l){for(Object x:l)noAlias(x,false);}}
 static Map<String,Object> nested(){return Map.of("operatorId","evil","child",Map.of("requestUser","evil2","safe","ok"),"list",List.of(Map.of("actorId","evil3","x",1)));}
 static BatchRuntimeCommandRequest command(String id,String reason){BatchRuntimeCommandRequest r=new BatchRuntimeCommandRequest();r.approvalRequestId=id;r.reason=reason;return r;}
 static BatchRuntimeDeploymentPlanRequest validPlan(){BatchRuntimeDeploymentPlanRequest r=new BatchRuntimeDeploymentPlanRequest();r.planId="P1";r.reason="deploy";r.manifest=Map.of("artifact","batch.jar","payload",nested());return r;}
 public static void main(String[] args){
   BatchRuntimeControlClient c=new BatchRuntimeControlClient(); AdmApprovalService approvals=new AdmApprovalService(); BatchRuntimeControlController ctl=new BatchRuntimeControlController(c,approvals);
   status(ctl.saveJobDefinition("session-admin",Map.of("jobId","J1","reason","r","payload",nested())),201); ok("session-admin".equals(c.last.get("requestedBy")),"server actor missing");noAlias(c.last,true);
   status(ctl.transitionJobDefinition("session-admin","J1",1,Map.of("targetState","PUBLISHED","reason","r","expectedVersion",1L,"approvalRequestId","A1","payload",nested())),200);noAlias(c.last,true);
   status(ctl.command("session-admin",command("101","approved stop")),202);ok(approvals.lastId==101L,"approval id not used");ok("session-admin".equals(approvals.lastOperator),"approval operator not server identity");
   status(ctl.plan("session-admin",validPlan()),201);noAlias(c.last,true);
   status(ctl.saveJobDefinition("session-admin",Map.of("reason","x")),400);
   status(ctl.transitionJobDefinition("session-admin","J",1,Map.of("targetState","PUBLISHED","reason","x","expectedVersion",-1L)),400);
   status(ctl.command("session-admin",command("bad","reason")),400);
   BatchRuntimeDeploymentPlanRequest badPlan=new BatchRuntimeDeploymentPlanRequest();badPlan.reason="deploy";status(ctl.plan("session-admin",badPlan),400);
   for(BatchControlClientException.Category cat:BatchControlClientException.Category.values()){
      c.failure=new BatchControlClientException(cat,"E_"+cat,"boom","trace",null);
      var r=ctl.plan("session-admin",validPlan());
      int expected=switch(cat){case VALIDATION->400;case PERMISSION->403;case NOT_FOUND->404;case CONFLICT->409;case UNKNOWN_RESULT->502;case UNAVAILABLE->503;case OWNER_ERROR->500;};
      status(r,expected);ok((cat==BatchControlClientException.Category.UNKNOWN_RESULT?"UNKNOWN_RESULT":"FAILED").equals(r.getBody().get("state")),"state mapping "+cat);
   }
   c.failure=new IllegalStateException("socket closed");var unknown=ctl.plan("session-admin",validPlan());status(unknown,503);ok("UNKNOWN_RESULT".equals(unknown.getBody().get("state")),"unexpected transport must be unknown");
   System.out.println("PASS assertions="+assertions);
 }
}
