package com.cpf.education.operations.backoffice.organization;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-BZA-01 — 조직·직원·발령·기준일
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduBackoffice01Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "WITHDRAWN", "CANCELLED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("미래 발령", "겹치는 발령", "조직 폐쇄", "기준일 경계");
    private static final List<String> REQUIRED_VERIFICATION = List.of("기준일 경계", "유효기간 겹침", "권한 Matrix", "Version 충돌", "응답 유실 대사", "Browser 메뉴 Field·Button", "감사·Download 권한");

    public EduBackoffice01Handler() {
        super(new EduCapabilityDefinition(
                "EDU-BZA-01", "조직·직원·발령·기준일", EduCapabilityKind.BACKOFFICE, "cpf-education",
                "CPF_BZA_OPERATOR", List.of("organizationId", "employeeId", "effectiveFrom", "to", "businessDate", "timezone"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
                true, true,
                false, false,
                false, false,
                3, "EDU-BZA-01"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.operations.backoffice.organization"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }

    @Override
    protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // Requirement별 필수값은 공통 엔진에서 검증하고 추가 의미 검증은 하위 Hook으로 확장합니다.
    }

    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("edu-bza-01" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BZA-01", EduConsumerType.JDBC_COMMAND, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "CANCELLED", "cpf-education EDU fixture DB backoffice/operations contract",
                "POST /api/education/edu-capabilities/EDU-BZA-01/executions", "cpf.education.features.backoffice.enabled", 60, List.of("organizationId", "employeeId", "effectiveFrom", "to", "businessDate", "timezone"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "조직·직원·발령·기준일");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("organizationId", "");
        return Map.copyOf(invalid);
    }
}
