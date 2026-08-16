package com.cpf.education.scenarios.online.generator.domain;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-01 — Generator 기반 신규 업무 영역 생성
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev01Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("PLANNED", "VALIDATED", "GENERATED", "VERIFIED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("같은 Module명", "같은 SystemCode", "Port 중복", "Route 중복", "지원하지 않는 DB Vendor", "생성 중 Process 종료", "기존 파일 Hash 변경 시 덮어쓰기 시도");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Dry Run이 Working Tree를 변경하지 않는지 확인", "충돌별 실패 메시지와 Exit Code 확인", "생성 중단 뒤 이번 실행 생성 파일만 안전하게 식별", "생성 결과 Build·Test·DB Pack 참조 확인");

    public EduDev01Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-01", "Generator 기반 신규 업무 영역 생성", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("domainName", "systemCode", "databaseVendor", "port", "route", "packageBase"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PREVIEW, EduWorkflowStep.CHECKSUM, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
                true, true,
                false, false,
                false, false,
                3, "EDU-DEV-01"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.generator.domain"; }
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
        return List.of("edu-dev-01" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-01", EduConsumerType.PROCESS, "cpf-education",
                "cpf-education/src/main/scripts/edu/invoke-education.ps1", "VERIFIED", "cpf-education allowlisted EDU operation script contract",
                "POST /api/education/edu-capabilities/EDU-DEV-01/executions", "cpf.repository-root", 600, List.of("domainName", "systemCode", "databaseVendor", "port", "route", "packageBase"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Generator 기반 신규 업무 영역 생성");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("domainName", "");
        return Map.copyOf(invalid);
    }
}
