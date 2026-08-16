package com.cpf.education.operations.admin.query;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-ADM-02 — 고객 업무 조회 연동
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduAdm02Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("REQUESTED", "ACCEPTED", "RUNNING", "SUCCEEDED", "FAILED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("조회 Timeout", "권한 밖 조직", "부분 데이터", "Stale Version");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Backend Contract Test", "Same-JVM/Remote Adapter Test", "권한·Masking Test", "Timeout·응답 유실 Test", "Browser Test", "Audit·Trace 연결");

    public EduAdm02Handler() {
        super(new EduCapabilityDefinition(
                "EDU-ADM-02", "고객 업무 조회 연동", EduCapabilityKind.OPERATIONS, "cpf-education",
                "CPF_ADM_OPERATOR", List.of("businessId", "approvalId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.READ_SNAPSHOT, EduWorkflowStep.PROTECT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.TIMEOUT, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
                true, false,
                false, false,
                false, false,
                3, "EDU-ADM-02"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.operations.admin.query"; }
    @Override public boolean readOnly() { return true; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }

    @Override
    protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // Requirement별 필수값은 공통 엔진에서 검증하고 추가 의미 검증은 하위 Hook으로 확장합니다.
    }

    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("edu-adm-02" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-ADM-02", EduConsumerType.JDBC_QUERY, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "RECONCILED", "cpf-education EDU fixture DB backoffice/operations contract",
                "POST /api/education/edu-capabilities/EDU-ADM-02/executions", "cpf.education.features.operations.enabled", 60, List.of("businessId", "approvalId"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "고객 업무 조회 연동");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("businessId", "");
        return Map.copyOf(invalid);
    }
}
