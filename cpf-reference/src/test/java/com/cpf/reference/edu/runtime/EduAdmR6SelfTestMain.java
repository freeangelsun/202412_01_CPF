package com.cpf.reference.edu.runtime;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import java.util.*;
public final class EduAdmR6SelfTestMain {
  private static void check(boolean v,String m){if(!v)throw new AssertionError(m);}
  private static AbstractEduCapabilityHandler h(String n)throws Exception{return (AbstractEduCapabilityHandler)Class.forName(n).getDeclaredConstructor().newInstance();}
  private static EduExecutionCommand cmd(Map<String,Object> payload){return cmd(payload,"documented operational reason","ORG:ORG1",1L,Set.of("CPF_ADM_OPERATOR"));}
  private static EduExecutionCommand cmd(Map<String,Object> payload,String reason,String scope,long version,Set<String> roles){return new EduExecutionCommand("BK-1","IDEM-1",version,"operator",roles,scope,reason,"REQ-1","TRACE-1",payload,EduFailurePoint.NONE,false,false);}
  private static void throwsType(Class<? extends Throwable> t,Runnable r,String m){try{r.run();}catch(Throwable x){if(t.isInstance(x))return;throw new AssertionError(m+" wrong exception "+x,x);}throw new AssertionError(m+" did not fail");}
  public static void main(String[] args)throws Exception {
    String[] classes={
      "com.cpf.reference.optional.operations.reuse.EduAdm01Handler",
      "com.cpf.reference.optional.operations.query.EduAdm02Handler",
      "com.cpf.reference.optional.operations.command.EduAdm03Handler",
      "com.cpf.reference.optional.operations.approval.EduAdm04Handler",
      "com.cpf.reference.optional.operations.asyncoperation.EduAdm05Handler",
      "com.cpf.reference.optional.operations.partialrecovery.EduAdm06Handler",
      "com.cpf.reference.optional.operations.customscreen.EduAdm07Handler",
      "com.cpf.reference.optional.operations.search.EduAdm08Handler",
      "com.cpf.reference.optional.operations.detail.EduAdm09Handler",
      "com.cpf.reference.optional.operations.bulk.EduAdm10Handler",
      "com.cpf.reference.optional.operations.configuration.EduAdm11Handler",
      "com.cpf.reference.optional.operations.incident.EduAdm12Handler",
      "com.cpf.reference.optional.operations.evidence.EduAdm13Handler",
      "com.cpf.reference.optional.operations.topology.EduAdm14Handler",
      "com.cpf.reference.optional.operations.correlation.EduAdm15Handler",
      "com.cpf.reference.optional.operations.notification.EduAdm16Handler",
      "com.cpf.reference.optional.operations.session.EduAdm17Handler"};
    for(String c:classes) check(h(c).definition().requiredRole().equals("CPF_ADM_OPERATOR"),c+" canonical role");
    check(h(classes[0]).readOnly(),"ADM01 readOnly");check(h(classes[1]).readOnly(),"ADM02 readOnly");check(!h(classes[2]).readOnly(),"ADM03 mutable");check(h(classes[13]).readOnly(),"ADM14 readOnly");check(h(classes[14]).readOnly(),"ADM15 readOnly");

    var p2=new LinkedHashMap<String,Object>();p2.put("businessId","B2");p2.put("approvalId","A2");p2.put("organizationId","ORG1");p2.put("subjectId","SUBJECT1234");p2.put("partialData",true);p2.put("observedVersion",1);p2.put("currentVersion",2);p2.put("pageSize",100);
    var h2=h(classes[1]);h2.validate(cmd(p2));var r2=h2.buildBusinessResult(cmd(p2),1);check(Boolean.TRUE.equals(r2.get("scopeEnforced")),"ADM02 scope");check(Boolean.TRUE.equals(r2.get("sensitiveFieldsMasked")),"ADM02 masking");check(Boolean.TRUE.equals(r2.get("partialData")),"ADM02 partial");check(Boolean.TRUE.equals(r2.get("staleVersion")),"ADM02 stale");check(Boolean.TRUE.equals(r2.get("reconcileRequired")),"ADM02 reconcile");
    throwsType(EduAuthorizationException.class,()->h2.validate(cmd(p2,"documented operational reason","ORG:OTHER",1,Set.of("CPF_ADM_OPERATOR"))),"ADM02 out-of-scope");

    var p3=new LinkedHashMap<String,Object>();p3.put("businessId","B3");p3.put("approvalId","A3");p3.put("currentVersion",1L);p3.put("currentState","READY");p3.put("requestedState","RUNNING");
    var h3=h(classes[2]);h3.validate(cmd(p3));var r3=h3.buildBusinessResult(cmd(p3),1);check(Boolean.TRUE.equals(r3.get("casEnforced")),"ADM03 CAS");check(Boolean.FALSE.equals(r3.get("blindRetryAllowed")),"ADM03 no blind retry");
    throwsType(EduValidationException.class,()->h3.validate(cmd(p3,"short","ORG:ORG1",1,Set.of("CPF_ADM_OPERATOR"))),"ADM03 reason");
    var conflict3=new LinkedHashMap<>(p3);conflict3.put("currentVersion",2L);throwsType(EduValidationException.class,()->h3.validate(cmd(conflict3)),"ADM03 version conflict");
    var state3=new LinkedHashMap<>(p3);state3.put("requestedState","INVALID");throwsType(EduValidationException.class,()->h3.validate(cmd(state3)),"ADM03 state transition");

    var p4=new LinkedHashMap<String,Object>();p4.put("businessId","B1");p4.put("approvalId","A1");p4.put("approvalPolicyId","P1");p4.put("requestedBy","u1");p4.put("approvedBy","u2");p4.put("approvalExpiresAtEpochMillis",System.currentTimeMillis()+60000);p4.put("approvedBusinessId","B1");p4.put("approvedTargetVersion",1);
    var h4=h(classes[3]); h4.validate(cmd(p4)); check(h4.targetKeys(cmd(p4)).equals(List.of("approval:A1:business:B1")),"ADM04 target binding");check(Boolean.TRUE.equals(h4.buildBusinessResult(cmd(p4),7).get("unknownResultRequiresReconcile")),"ADM04 reconcile semantics");
    var sod=new LinkedHashMap<>(p4);sod.put("approvedBy","u1");throwsType(EduAuthorizationException.class,()->h4.validate(cmd(sod)),"ADM04 SoD");
    throwsType(EduAuthorizationException.class,()->h4.validate(cmd(p4,"documented operational reason","ORG:ORG1",1,Set.of("CPF_REFERENCE_PLATFORM_OPERATOR"))),"legacy role must not authorize");

    var p5=new LinkedHashMap<String,Object>();p5.put("businessId","B5");p5.put("approvalId","A5");p5.put("operationId","OP5");p5.put("responseLost",true);p5.put("pollAttempt",1);
    var h5=h(classes[4]);h5.validate(cmd(p5));var r5=h5.buildBusinessResult(cmd(p5),1);check("UNKNOWN_RESULT".equals(r5.get("businessState")),"ADM05 unknown");check("OP5".equals(r5.get("operationId")),"ADM05 durable operation id");check(Boolean.TRUE.equals(r5.get("reconcileRequired")),"ADM05 reconcile");check(Boolean.FALSE.equals(r5.get("blindRetryAllowed")),"ADM05 no blind retry");

    var p6=new LinkedHashMap<String,Object>();p6.put("businessId","B6");p6.put("approvalId","A6");p6.put("targetIds",List.of("T1","T2","T3"));p6.put("failedTargetIds",List.of("T2"));
    var h6=h(classes[5]);h6.validate(cmd(p6));var r6=h6.buildBusinessResult(cmd(p6),1);check("RECONCILED".equals(r6.get("businessState")),"ADM06 partial reconciled");check(r6.get("reprocessTargetIds").equals(List.of("target:T2")),"ADM06 failed-only reprocess");check(Boolean.FALSE.equals(r6.get("successfulTargetsReplayAllowed")),"ADM06 no success replay");check(Boolean.TRUE.equals(r6.get("compensationAvailable")),"ADM06 compensation");
    var bad6=new LinkedHashMap<>(p6);bad6.put("failedTargetIds",List.of("T9"));throwsType(EduValidationException.class,()->h6.validate(cmd(bad6)),"ADM06 failed subset");

    var p7=new LinkedHashMap<String,Object>();p7.put("businessId","B7");p7.put("approvalId","A7");p7.put("frontendContractVersion","v1");p7.put("backendContractVersion","v1");
    var h7=h(classes[6]);h7.validate(cmd(p7));var r7=h7.buildBusinessResult(cmd(p7),1);check(Boolean.TRUE.equals(r7.get("generatedClientRequired")),"ADM07 generated client");check(Boolean.FALSE.equals(r7.get("directDbMutationAllowed")),"ADM07 direct DB forbidden");
    var reuse7=new LinkedHashMap<>(p7);reuse7.put("existingCapabilityAvailable",true);throwsType(EduValidationException.class,()->h7.validate(cmd(reuse7)),"ADM07 reuse-first");
    var direct7=new LinkedHashMap<>(p7);direct7.put("directDbMutation",true);throwsType(EduAuthorizationException.class,()->h7.validate(cmd(direct7)),"ADM07 no direct DB");
    var drift7=new LinkedHashMap<>(p7);drift7.put("frontendContractVersion","v2");throwsType(EduValidationException.class,()->h7.validate(cmd(drift7)),"ADM07 contract drift");

    var p8=new LinkedHashMap<String,Object>();p8.put("permission","MASKED");p8.put("approvalId","A8");p8.put("organizationId","ORG1");p8.put("subjectId","SUBJECT1234");
    var h8=h(classes[7]);h8.validate(cmd(p8));var r8=h8.buildBusinessResult(cmd(p8),1);check(Boolean.TRUE.equals(r8.get("idorScopeEnforced")),"ADM08 IDOR flag");check(!Objects.equals(r8.get("subjectId"),"SUBJECT1234"),"ADM08 masked subject");
    throwsType(EduAuthorizationException.class,()->h8.validate(cmd(p8,"documented operational reason","ORG:OTHER",1,Set.of("CPF_ADM_OPERATOR"))),"ADM08 org IDOR");

    var p9=new LinkedHashMap<String,Object>();p9.put("resourceId","R9");p9.put("action","SAVE");p9.put("currentVersion",2L);
    var h9=h(classes[8]);h9.validate(cmd(p9));var r9=h9.buildBusinessResult(cmd(p9),1);check("CONFLICT".equals(r9.get("businessState")),"ADM09 conflict state");check(Boolean.FALSE.equals(r9.get("blindRetryAllowed")),"ADM09 no blind retry");

    var p10=new LinkedHashMap<String,Object>();p10.put("targetIds",List.of("T1","T2"));p10.put("command","UPDATE");p10.put("expectedVersions",List.of(1,1));p10.put("fileName","result.csv");p10.put("contentLength",10L);p10.put("checksum","a".repeat(64));p10.put("failedTargetIds",List.of("T2"));
    var h10=h(classes[9]);h10.validate(cmd(p10));var r10=h10.buildBusinessResult(cmd(p10),1);check("PARTIAL".equals(r10.get("businessState")),"ADM10 partial state");check(r10.get("reprocessTargetIds").equals(List.of("T2")),"ADM10 reprocess failed only");check(Boolean.FALSE.equals(r10.get("successfulTargetsReplayAllowed")),"ADM10 no successful replay");

    var p11=new LinkedHashMap<String,Object>();p11.put("configVersion","v2");p11.put("targets",List.of("NODE1","NODE2"));p11.put("maintenanceWindow","MW1");p11.put("maintenanceWindowOpen",true);p11.put("expectedChecksum","abc");p11.put("actualChecksum","abc");p11.put("partialFailure",true);p11.put("lastKnownGoodVersion","v1");
    var h11=h(classes[10]);h11.validate(cmd(p11));var r11=h11.buildBusinessResult(cmd(p11),1);check("PARTIAL".equals(r11.get("businessState")),"ADM11 partial");check("v1".equals(r11.get("lastKnownGoodVersion")),"ADM11 LKG");check(Boolean.TRUE.equals(r11.get("rollbackAvailable")),"ADM11 rollback");
    var bad11=new LinkedHashMap<>(p11);bad11.put("maintenanceWindowOpen",false);throwsType(EduValidationException.class,()->h11.validate(cmd(bad11)),"ADM11 window");

    var p12=new LinkedHashMap<String,Object>();p12.put("incidentId","I12");p12.put("transactionIds",List.of("TX1","TX2"));p12.put("severity","P1");p12.put("owner","OPS");p12.put("recoveryAuthorized",true);p12.put("recoveryCompleted",true);
    var h12=h(classes[11]);h12.validate(cmd(p12));var r12=h12.buildBusinessResult(cmd(p12),1);check("CLOSED".equals(r12.get("businessState")),"ADM12 recovered close");check(Boolean.TRUE.equals(r12.get("evidenceRequiredBeforeClose")),"ADM12 evidence");

    var p13=new LinkedHashMap<String,Object>();p13.put("filters","date=today");p13.put("format","csv");p13.put("approvalId","A13");p13.put("fileName","audit.csv");p13.put("contentLength",20L);p13.put("checksum","b".repeat(64));p13.put("approvalPolicyId","P13");
    var h13=h(classes[12]);h13.validate(cmd(p13));var r13=h13.buildBusinessResult(cmd(p13),1);check(Boolean.TRUE.equals(r13.get("approvalBoundExport")),"ADM13 approval-bound export");check(Boolean.FALSE.equals(r13.get("rawPayloadIncludedInResult")),"ADM13 raw excluded");

    var p14=new LinkedHashMap<String,Object>();p14.put("serviceId","S14");p14.put("instanceId","N14");p14.put("timeRange","1h");p14.put("observedAtEpochMillis",System.currentTimeMillis()-120000);p14.put("freshnessSlaMillis",1000L);
    var h14=h(classes[13]);h14.validate(cmd(p14));check(Boolean.TRUE.equals(h14.buildBusinessResult(cmd(p14),1).get("stale")),"ADM14 stale health");

    var p15=new LinkedHashMap<String,Object>();p15.put("transactionId","TX15");p15.put("segmentId","SEG15");p15.put("timeRange","1h");p15.put("organizationId","ORG1");p15.put("pageSize",100);p15.put("partialResult",true);
    var h15=h(classes[14]);h15.validate(cmd(p15));var r15=h15.buildBusinessResult(cmd(p15),1);check("PARTIAL".equals(r15.get("businessState")),"ADM15 partial trace");check(Boolean.TRUE.equals(r15.get("sensitiveFieldsMasked")),"ADM15 masking");

    var p16=new LinkedHashMap<String,Object>();p16.put("alertId","AL16");p16.put("ackReason","acknowledged after verification");p16.put("owner","next-shift");p16.put("snoozeUntil",java.time.Instant.now().plusSeconds(300).toString());p16.put("destination","ops-topic");p16.put("messageKey","AL16");p16.put("escalate",true);
    var h16=h(classes[15]);h16.validate(cmd(p16));var r16=h16.buildBusinessResult(cmd(p16),1);check("ESCALATED".equals(r16.get("businessState")),"ADM16 escalation");check(Boolean.TRUE.equals(r16.get("duplicateAckConverges")),"ADM16 ack idempotency");

    var p17=new LinkedHashMap<String,Object>();p17.put("sessionId","SES17");p17.put("commandId","CMD17");p17.put("sessionExpired",true);p17.put("postSent",true);p17.put("reauthenticated",false);p17.put("csrfValid",true);p17.put("sameOrigin",true);
    var h17=h(classes[16]);h17.validate(cmd(p17));var r17=h17.buildBusinessResult(cmd(p17),1);check("SESSION_EXPIRED".equals(r17.get("businessState")),"ADM17 expiry");check(Boolean.FALSE.equals(r17.get("autoReplayAllowed")),"ADM17 no replay");check(Boolean.TRUE.equals(r17.get("commandReconcileRequired")),"ADM17 unknown post reconcile");
    var csrf=new LinkedHashMap<>(p17);csrf.put("csrfValid",false);throwsType(EduAuthorizationException.class,()->h17.validate(cmd(csrf)),"ADM17 csrf");

    System.out.println("[CPF][R6I][EDU-ADM][SELFTEST][PASS] handlers=17 semantics=query,cas,approval,async,partial-recovery,custom-screen,idor,bulk,lkg,recovery,evidence,topology,trace,notification,session");
  }
}
