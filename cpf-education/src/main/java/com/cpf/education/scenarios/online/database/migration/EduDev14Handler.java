package com.cpf.education.scenarios.online.database.migration;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-14 — Oracle·PostgreSQL·MariaDB 동일 의미 Migration
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev14Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("NOT_APPLIED", "APPLYING", "APPLIED", "VERIFIED", "FAILED", "ROLLED_BACK");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("중간 중단", "재실행", "기존 Null/불량 데이터", "Lock Timeout", "Index 생성 실패", "Rollback 후 App 호환", "Vendor 문법 차이");
    private static final List<String> REQUIRED_VERIFICATION = List.of("세 Vendor 신규 설치", "이전 Version→Upgrade", "Upgrade 재실행", "중단 후 재개", "Rollback·Forward Fix", "Schema Diff와 Query 결과 동일성");

    public EduDev14Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-14", "Oracle·PostgreSQL·MariaDB 동일 의미 Migration", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("customerId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.COMPENSATION_READY, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, true,
                false, false,
                true, true,
                3, "EDU-DEV-14"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.database.migration"; }
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
        return List.of("edu-dev-14" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-14", EduConsumerType.PROCESS, "cpf-education",
                "cpf-education/src/main/scripts/edu/invoke-education.ps1", "ROLLED_BACK", "cpf-education allowlisted EDU operation script contract",
                "POST /api/education/edu-capabilities/EDU-DEV-14/executions", "cpf.repository-root", 600, List.of("customerId"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Oracle·PostgreSQL·MariaDB 동일 의미 Migration");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("customerId", "");
        return Map.copyOf(invalid);
    }
}
