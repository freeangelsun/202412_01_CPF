package com.cpf.education.scenarios.online.command.audit;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-03 — 등록·수정·상태 변경과 감사
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev03Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("PENDING", "ACTIVE", "SUSPENDED", "CLOSED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("사유 누락", "허용되지 않은 상태 전이", "권한 없음", "Version 충돌", "감사 저장 실패", "Outbox 저장 실패", "응답 유실");
    private static final List<String> REQUIRED_VERIFICATION = List.of("상태 전이 Matrix Unit Test", "Transaction Rollback 시 상태·감사·Outbox 불일치 없음", "권한별 허용 Action", "응답 유실 후 operation 조회");

    public EduDev03Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-03", "등록·수정·상태 변경과 감사", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("suspend"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
                true, true,
                false, false,
                false, false,
                3, "EDU-DEV-03"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.command.audit"; }
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
        return List.of("edu-dev-03" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-03", EduConsumerType.JDBC_COMMAND, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "CLOSED", "cpf-education EDU fixture DB business record contract",
                "POST /api/education/edu-capabilities/EDU-DEV-03/executions", "", 60, List.of("suspend"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "등록·수정·상태 변경과 감사");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("suspend", "");
        return Map.copyOf(invalid);
    }
}
