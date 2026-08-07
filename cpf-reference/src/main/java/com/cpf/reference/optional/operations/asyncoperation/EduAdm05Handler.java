package com.cpf.reference.optional.operations.asyncoperation;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/** EDU-ADM-05 — 비동기 작업·응답 유실: durable operation identity and UNKNOWN reconciliation. */
public final class EduAdm05Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES=List.of("REQUESTED","ACCEPTED","RUNNING","SUCCEEDED","FAILED","UNKNOWN_RESULT","RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS=List.of("접수 응답 유실","Polling 실패","중복 Operation","UNKNOWN_RESULT");
    private static final List<String> REQUIRED_VERIFICATION=List.of("Backend Contract Test","Same-JVM/Remote Adapter Test","권한·Masking Test","Timeout·응답 유실 Test","Browser Test","Audit·Trace 연결");
    public EduAdm05Handler(){super(new EduCapabilityDefinition("EDU-ADM-05","비동기 작업·응답 유실",EduCapabilityKind.OPERATIONS,"cpf-reference","CPF_REFERENCE_PLATFORM_OPERATOR",List.of("businessId","approvalId"),List.of(EduWorkflowStep.VALIDATE,EduWorkflowStep.AUTHORIZE,EduWorkflowStep.SCOPE,EduWorkflowStep.DEDUPE,EduWorkflowStep.VERSION_CHECK,EduWorkflowStep.PROTECT,EduWorkflowStep.MUTATE,EduWorkflowStep.COMMIT,EduWorkflowStep.OUTBOX,EduWorkflowStep.EXTERNAL_SEND,EduWorkflowStep.ACK,EduWorkflowStep.RECONCILE,EduWorkflowStep.AUDIT,EduWorkflowStep.OBSERVE),Set.of(EduFailurePoint.BEFORE_COMMIT,EduFailurePoint.AFTER_COMMIT,EduFailurePoint.BEFORE_EXTERNAL_SEND,EduFailurePoint.AFTER_EXTERNAL_SEND,EduFailurePoint.RESPONSE_LOST,EduFailurePoint.TIMEOUT,EduFailurePoint.PROCESS_KILL),true,true,false,true,false,false,5,"EDU-ADM-05"));}
    @Override public String implementationPackage(){return "com.cpf.reference.optional.operations.asyncoperation";}
    @Override public boolean readOnly(){return false;}
    @Override public List<String> businessStates(){return BUSINESS_STATES;}
    @Override public List<String> exceptionScenarios(){return EXCEPTION_SCENARIOS;}
    @Override public List<String> requiredVerification(){return REQUIRED_VERIFICATION;}
    @Override protected void validateBusinessInput(EduExecutionCommand c){super.validateBusinessInput(c);Object poll=c.payload().get("pollAttempt");if(poll!=null&&Long.parseLong(String.valueOf(poll))<0)throw new EduValidationException("pollAttempt must be >= 0");}
    @Override public List<String> targetKeys(EduExecutionCommand c){String operation=String.valueOf(c.payload().getOrDefault("operationId",c.idempotencyKey()));return List.of("async-operation:"+operation);}
    @Override public EduConsumerBinding consumerBinding(){return new EduConsumerBinding("EDU-ADM-05",EduConsumerType.JDBC_COMMAND,"cpf-reference","CPF_EDU_BUSINESS_RECORD","RECONCILED","cpf-reference REF DB backoffice/operations contract","POST /api/reference/edu-capabilities/EDU-ADM-05/executions","cpf.reference.features.operations.enabled",60,List.of("businessId","approvalId"));}
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand c,long f){Map<String,Object> r=new LinkedHashMap<>(super.buildBusinessResult(c,f));boolean unknown=Set.of(EduFailurePoint.AFTER_COMMIT,EduFailurePoint.AFTER_EXTERNAL_SEND,EduFailurePoint.RESPONSE_LOST,EduFailurePoint.TIMEOUT,EduFailurePoint.PROCESS_KILL).contains(c.failurePoint())||Boolean.TRUE.equals(c.payload().get("responseLost"));r.put("scenarioTitle","비동기 작업·응답 유실");r.put("operationId",String.valueOf(c.payload().getOrDefault("operationId",c.idempotencyKey())));r.put("businessState",unknown?"UNKNOWN_RESULT":"SUCCEEDED");r.put("duplicateOperationConverges",true);r.put("blindRetryAllowed",false);r.put("pollingBounded",true);r.put("reconcileRequired",unknown);r.put("targetKeys",targetKeys(c));return Map.copyOf(r);}
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> v){Map<String,Object> i=new LinkedHashMap<>(v);i.put("businessId","");return Map.copyOf(i);}
}
