package com.cpf.education.scenarios.online.concurrency.optimisticlock;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-04 — 동시 수정과 예상 Version 충돌
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev04Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("VERSION_CONFLICT");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("동일 Version 동시 요청", "오래된 Version", "재시도 중 또 다른 변경", "응답 유실 후 성공 여부 불명", "DB Deadlock");
    private static final List<String> REQUIRED_VERIFICATION = List.of("실제 동시 Thread/Request로 1건 성공·1건 충돌", "세 DB Vendor에서 같은 의미", "충돌 요청이 감사 성공으로 기록되지 않음", "성공 응답 유실 후 Operation 대사");

    public EduDev04Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-04", "동시 수정과 예상 Version 충돌", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("limitId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
                true, true,
                false, false,
                false, false,
                3, "EDU-DEV-04"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.concurrency.optimisticlock"; }
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
        return List.of("edu-dev-04" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-04", EduConsumerType.JDBC_COMMAND, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "VERSION_CONFLICT", "cpf-education EDU fixture DB business record contract",
                "POST /api/education/edu-capabilities/EDU-DEV-04/executions", "", 60, List.of("limitId"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "동시 수정과 예상 Version 충돌");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("limitId", "");
        return Map.copyOf(invalid);
    }
}
