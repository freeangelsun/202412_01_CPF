package com.cpf.education.scenarios.online.servicecall.topology;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-06 — 같은 애플리케이션·분리 서비스 호출 동등성
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev06Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UNKNOWN_RESULT");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Remote 연결 실패", "응답 Timeout", "잘못된 JSON", "하위 서비스 4xx/5xx", "호출 후 응답 유실", "서비스 주소 변경", "다중 인스턴스 일부 장애");
    private static final List<String> REQUIRED_VERIFICATION = List.of("같은 Contract Test를 Local/Remote Adapter에 적용", "오류 코드·상태 동일성", "Trace Parent 전달", "시간 예산 초과", "Load Balancing과 장애 인스턴스 제외");

    public EduDev06Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-06", "같은 애플리케이션·분리 서비스 호출 동등성", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("contractId", "member"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
                true, false,
                false, true,
                false, false,
                5, "EDU-DEV-06"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.servicecall.topology"; }
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
        return List.of("edu-dev-06" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-06", EduConsumerType.HTTP, "cpf-education",
                "/external/06", "UNKNOWN_RESULT", "cpf-education counterparty simulator HTTP contract",
                "POST /api/education/edu-capabilities/EDU-DEV-06/executions", "cpf.edu.counterparty.base-url", 60, List.of("contractId", "member"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "같은 애플리케이션·분리 서비스 호출 동등성");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("contractId", "");
        return Map.copyOf(invalid);
    }
}
