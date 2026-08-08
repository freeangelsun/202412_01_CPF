package com.cpf.reference.optional.operations.query;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/** EDU-ADM-02 — 고객 업무 조회 연동: scope/masking/partial/stale query semantics. */
public final class EduAdm02Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES=List.of("REQUESTED","ACCEPTED","RUNNING","SUCCEEDED","FAILED","UNKNOWN_RESULT","RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS=List.of("조회 Timeout","권한 밖 조직","부분 데이터","Stale Version");
    private static final List<String> REQUIRED_VERIFICATION=List.of("Backend Contract Test","Same-JVM/Remote Adapter Test","권한·Masking Test","Timeout·응답 유실 Test","Browser Test","Audit·Trace 연결");
    public EduAdm02Handler(){super(new EduCapabilityDefinition("EDU-ADM-02","고객 업무 조회 연동",EduCapabilityKind.OPERATIONS,"cpf-reference","CPF_ADM_OPERATOR",List.of("businessId","approvalId"),List.of(EduWorkflowStep.VALIDATE,EduWorkflowStep.AUTHORIZE,EduWorkflowStep.SCOPE,EduWorkflowStep.DEDUPE,EduWorkflowStep.READ_SNAPSHOT,EduWorkflowStep.PROTECT,EduWorkflowStep.RECONCILE,EduWorkflowStep.AUDIT,EduWorkflowStep.OBSERVE),Set.of(EduFailurePoint.TIMEOUT,EduFailurePoint.RESPONSE_LOST,EduFailurePoint.PARTIAL_TARGET_FAILURE),true,false,false,false,false,false,3,"EDU-ADM-02"));}
    @Override public String implementationPackage(){return "com.cpf.reference.optional.operations.query";}
    @Override public boolean readOnly(){return true;}
    @Override public List<String> businessStates(){return BUSINESS_STATES;}
    @Override public List<String> exceptionScenarios(){return EXCEPTION_SCENARIOS;}
    @Override public List<String> requiredVerification(){return REQUIRED_VERIFICATION;}
    @Override protected void validateBusinessInput(EduExecutionCommand c){
        super.validateBusinessInput(c);
        Object org=c.payload().get("organizationId");
        if(org!=null && !scopeContainsOrganization(c.dataScope(),String.valueOf(org))) throw new EduAuthorizationException("organization is outside dataScope");
        Object pageSize=c.payload().get("pageSize");
        if(pageSize!=null){long v=Long.parseLong(String.valueOf(pageSize));if(v<1||v>500)throw new EduValidationException("pageSize must be 1..500");}
    }
    private static boolean scopeContainsOrganization(String scope,String org){return scope.equals("ORG:"+org)||scope.equals("ORG:*")||scope.equals("GLOBAL");}
    @Override public List<String> targetKeys(EduExecutionCommand c){return List.of("query:"+c.payload().get("businessId")+":scope:"+c.dataScope());}
    @Override public EduConsumerBinding consumerBinding(){return new EduConsumerBinding("EDU-ADM-02",EduConsumerType.JDBC_QUERY,"cpf-reference","CPF_EDU_BUSINESS_RECORD","RECONCILED","cpf-reference REF DB backoffice/operations contract","POST /api/reference/edu-capabilities/EDU-ADM-02/executions","cpf.reference.features.operations.enabled",60,List.of("businessId","approvalId"));}
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand c,long f){
        Map<String,Object> r=new LinkedHashMap<>(super.buildBusinessResult(c,f));boolean partial=Boolean.TRUE.equals(c.payload().get("partialData"));
        Object observed=c.payload().get("observedVersion"), current=c.payload().get("currentVersion");boolean stale=observed!=null&&current!=null&&!Objects.equals(String.valueOf(observed),String.valueOf(current));
        String subject=String.valueOf(c.payload().getOrDefault("subjectId",""));String masked=subject.length()<4?"****":"***"+subject.substring(subject.length()-4);
        r.put("scenarioTitle","고객 업무 조회 연동");r.put("businessState",partial?"RECONCILED":"SUCCEEDED");r.put("readOnly",true);r.put("scopeEnforced",true);r.put("sensitiveFieldsMasked",true);r.put("maskedSubjectId",masked);r.put("partialData",partial);r.put("staleVersion",stale);r.put("reconcileRequired",partial||stale||c.failurePoint()==EduFailurePoint.RESPONSE_LOST||c.failurePoint()==EduFailurePoint.TIMEOUT);r.put("targetKeys",targetKeys(c));return Map.copyOf(r);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> v){Map<String,Object> i=new LinkedHashMap<>(v);i.put("businessId","");return Map.copyOf(i);}
}
