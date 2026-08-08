package com.cpf.reference.optional.operations.command;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/** EDU-ADM-03 — 안전한 운영 조치: reason/CAS/state/reconcile semantics. */
public final class EduAdm03Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES=List.of("REQUESTED","ACCEPTED","RUNNING","SUCCEEDED","FAILED","UNKNOWN_RESULT","RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS=List.of("사유 누락","Version 충돌","허용 상태 아님","응답 유실");
    private static final List<String> REQUIRED_VERIFICATION=List.of("Backend Contract Test","Same-JVM/Remote Adapter Test","권한·Masking Test","Timeout·응답 유실 Test","Browser Test","Audit·Trace 연결");
    public EduAdm03Handler(){super(new EduCapabilityDefinition("EDU-ADM-03","안전한 운영 조치",EduCapabilityKind.OPERATIONS,"cpf-reference","CPF_ADM_OPERATOR",List.of("businessId","approvalId"),List.of(EduWorkflowStep.VALIDATE,EduWorkflowStep.AUTHORIZE,EduWorkflowStep.SCOPE,EduWorkflowStep.DEDUPE,EduWorkflowStep.VERSION_CHECK,EduWorkflowStep.PROTECT,EduWorkflowStep.MUTATE,EduWorkflowStep.COMMIT,EduWorkflowStep.RECONCILE,EduWorkflowStep.AUDIT,EduWorkflowStep.OBSERVE),Set.of(EduFailurePoint.BEFORE_COMMIT,EduFailurePoint.AFTER_COMMIT),true,true,false,false,false,false,3,"EDU-ADM-03"));}
    @Override public String implementationPackage(){return "com.cpf.reference.optional.operations.command";}
    @Override public boolean readOnly(){return false;}
    @Override public List<String> businessStates(){return BUSINESS_STATES;}
    @Override public List<String> exceptionScenarios(){return EXCEPTION_SCENARIOS;}
    @Override public List<String> requiredVerification(){return REQUIRED_VERIFICATION;}
    @Override protected void validateBusinessInput(EduExecutionCommand c){
        super.validateBusinessInput(c);if(c.requestReason().trim().length()<8)throw new EduValidationException("requestReason must be at least 8 characters");
        Object currentVersion=c.payload().get("currentVersion");if(currentVersion!=null&&Long.parseLong(String.valueOf(currentVersion))!=c.expectedVersion())throw new EduValidationException("expectedVersion conflict");
        Object current=c.payload().get("currentState"), requested=c.payload().get("requestedState");
        if(current!=null&&requested!=null&&!allowed(String.valueOf(current),String.valueOf(requested)))throw new EduValidationException("state transition not allowed");
    }
    private static boolean allowed(String current,String requested){return switch(current){case "READY"->Set.of("RUNNING","SUSPENDED").contains(requested);case "RUNNING"->Set.of("SUSPENDED","STOPPED").contains(requested);case "SUSPENDED"->Set.of("RUNNING","STOPPED").contains(requested);default->false;};}
    @Override public List<String> targetKeys(EduExecutionCommand c){return List.of("command:"+c.payload().get("businessId")+":v"+c.expectedVersion());}
    @Override public EduConsumerBinding consumerBinding(){return new EduConsumerBinding("EDU-ADM-03",EduConsumerType.JDBC_COMMAND,"cpf-reference","CPF_EDU_BUSINESS_RECORD","RECONCILED","cpf-reference REF DB backoffice/operations contract","POST /api/reference/edu-capabilities/EDU-ADM-03/executions","cpf.reference.features.operations.enabled",60,List.of("businessId","approvalId"));}
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand c,long f){Map<String,Object> r=new LinkedHashMap<>(super.buildBusinessResult(c,f));boolean unknown=c.failurePoint()==EduFailurePoint.AFTER_COMMIT;r.put("scenarioTitle","안전한 운영 조치");r.put("businessState",unknown?"UNKNOWN_RESULT":"SUCCEEDED");r.put("reasonRecorded",true);r.put("casEnforced",true);r.put("blindRetryAllowed",false);r.put("reconcileRequired",unknown);r.put("targetKeys",targetKeys(c));return Map.copyOf(r);}
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> v){Map<String,Object> i=new LinkedHashMap<>(v);i.put("businessId","");return Map.copyOf(i);}
}
