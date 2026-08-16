package com.cpf.education.scenarios.online.resilience.recovery;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-15 — 지급 업무 장애 주입·복구·운영 인계
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev15Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("RECEIVED", "VALIDATED", "APPLIED", "SUCCEEDED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Process Kill", "DB Proxy 차단", "Kafka Proxy 차단", "외부 지연/Reset", "Disk Full 모의", "응답 Drop", "다중 인스턴스 Lease 상실");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Scenario 자동 반복", "중복 Side Effect 검사", "Recovery 전후 상태", "Log·Metric·Trace·Audit Evidence", "재실행 시 동일 결과", "운영 Runbook 명령 검증");

    public EduDev15Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-15", "지급 업무 장애 주입·복구·운영 인계", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("amount", "currency"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, false,
                false, false,
                false, false,
                3, "EDU-DEV-15"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.resilience.recovery"; }
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
        return List.of("edu-dev-15" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-15", EduConsumerType.JDBC_COMMAND, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "SUCCEEDED", "cpf-education EDU fixture DB business record contract",
                "POST /api/education/edu-capabilities/EDU-DEV-15/executions", "", 60, List.of("amount", "currency"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "지급 업무 장애 주입·복구·운영 인계");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("amount", "");
        return Map.copyOf(invalid);
    }
}
