package com.cpf.reference.optional.operations.customscreen;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/** EDU-ADM-07 — 고객 전용 화면 추가의 마지막 선택: reuse-first and generated-contract enforcement. */
public final class EduAdm07Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES=List.of("REQUESTED","ACCEPTED","RUNNING","SUCCEEDED","FAILED","UNKNOWN_RESULT","RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS=List.of("기존 기능 중복","직접 DB 수정","권한 누락","Frontend/Backend 계약 불일치");
    private static final List<String> REQUIRED_VERIFICATION=List.of("Backend Contract Test","Same-JVM/Remote Adapter Test","권한·Masking Test","Timeout·응답 유실 Test","Browser Test","Audit·Trace 연결");
    public EduAdm07Handler(){super(new EduCapabilityDefinition("EDU-ADM-07","고객 전용 화면 추가의 마지막 선택",EduCapabilityKind.OPERATIONS,"cpf-reference","CPF_REFERENCE_PLATFORM_OPERATOR",List.of("businessId","approvalId"),List.of(EduWorkflowStep.VALIDATE,EduWorkflowStep.AUTHORIZE,EduWorkflowStep.SCOPE,EduWorkflowStep.DEDUPE,EduWorkflowStep.VERSION_CHECK,EduWorkflowStep.PROTECT,EduWorkflowStep.MUTATE,EduWorkflowStep.COMMIT,EduWorkflowStep.RECONCILE,EduWorkflowStep.AUDIT,EduWorkflowStep.OBSERVE),Set.of(EduFailurePoint.BEFORE_COMMIT,EduFailurePoint.AFTER_COMMIT),true,true,false,false,false,false,3,"EDU-ADM-07"));}
    @Override public String implementationPackage(){return "com.cpf.reference.optional.operations.customscreen";}
    @Override public boolean readOnly(){return false;}
    @Override public List<String> businessStates(){return BUSINESS_STATES;}
    @Override public List<String> exceptionScenarios(){return EXCEPTION_SCENARIOS;}
    @Override public List<String> requiredVerification(){return REQUIRED_VERIFICATION;}
    @Override protected void validateBusinessInput(EduExecutionCommand c){super.validateBusinessInput(c);if(Boolean.TRUE.equals(c.payload().get("existingCapabilityAvailable")))throw new EduValidationException("reuse existing ADM capability instead of custom screen");if(Boolean.TRUE.equals(c.payload().get("directDbMutation")))throw new EduAuthorizationException("direct DB mutation is forbidden");Object front=c.payload().get("frontendContractVersion"),back=c.payload().get("backendContractVersion");if(front!=null&&back!=null&&!Objects.equals(String.valueOf(front),String.valueOf(back)))throw new EduValidationException("Frontend/Backend contract version mismatch");}
    @Override public List<String> targetKeys(EduExecutionCommand c){return List.of("custom-screen:"+c.payload().get("businessId")+":v"+c.expectedVersion());}
    @Override public EduConsumerBinding consumerBinding(){return new EduConsumerBinding("EDU-ADM-07",EduConsumerType.JDBC_COMMAND,"cpf-reference","CPF_EDU_BUSINESS_RECORD","RECONCILED","cpf-reference REF DB backoffice/operations contract","POST /api/reference/edu-capabilities/EDU-ADM-07/executions","cpf.reference.features.operations.enabled",60,List.of("businessId","approvalId"));}
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand c,long f){Map<String,Object> r=new LinkedHashMap<>(super.buildBusinessResult(c,f));r.put("scenarioTitle","고객 전용 화면 추가의 마지막 선택");r.put("businessState","SUCCEEDED");r.put("reuseDecisionRecorded",true);r.put("generatedClientRequired",true);r.put("directDbMutationAllowed",false);r.put("routePermissionRequired",true);r.put("backendContractVersion",String.valueOf(c.payload().getOrDefault("backendContractVersion","current")));r.put("targetKeys",targetKeys(c));return Map.copyOf(r);}
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> v){Map<String,Object> i=new LinkedHashMap<>(v);i.put("businessId","");return Map.copyOf(i);}
}
