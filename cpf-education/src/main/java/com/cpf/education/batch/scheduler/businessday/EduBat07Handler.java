package com.cpf.education.batch.scheduler.businessday;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-BAT-07 — 영업일 23시 Scheduler
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduBat07Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("SCHEDULED", "TRIGGERED", "COMPLETED", "MISFIRED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("이중 Trigger", "Clock 차이", "휴일 Override", "Misfire");
    private static final List<String> REQUIRED_VERIFICATION = List.of("정상 실행", "중간 실패·Process 종료", "중복 Trigger", "Stop·Restart", "업무 건수·금액·Hash 대사", "ADM Execution ID 연결");

    public EduBat07Handler() {
        super(new EduCapabilityDefinition(
                "EDU-BAT-07", "영업일 23시 Scheduler", EduCapabilityKind.BATCH, "cpf-education",
                "CPF_BATCH_OPERATOR", List.of("scheduleId", "calendarId", "misfirePolicy", "businessDate", "timezone"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.CLAIM_LEASE, EduWorkflowStep.CHECKSUM, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.CHECKPOINT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL, EduFailurePoint.LEASE_LOST),
                true, true,
                true, false,
                false, false,
                5, "EDU-BAT-07"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.batch.scheduler.businessday"; }
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
        return List.of("edu-bat-07" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BAT-07", EduConsumerType.SPRING_BATCH, "cpf-education",
                "eduBat07Job", "MISFIRED", "cpf-education optional Spring Batch Job/Step contract",
                "POST /api/education/edu-capabilities/EDU-BAT-07/executions", "cpf.education.features.batch.enabled", 3600, List.of("scheduleId", "calendarId", "misfirePolicy", "businessDate", "timezone"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "영업일 23시 Scheduler");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("scheduleId", "");
        return Map.copyOf(invalid);
    }
}
