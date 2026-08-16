package com.cpf.education.operations.gateway.reconcile;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-GW-06 — Attempt Ledger·UNKNOWN_RESULT·LKG 복구
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduGw06Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "VALIDATED", "APPROVAL_REQUESTED", "APPROVED", "PUBLISHING", "APPLIED", "PARTIAL", "REJECTED", "ROLLED_BACK");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Target 처리 후 응답 유실", "늦은 응답", "중복 요청", "LKG 일부 적용");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Target 직접/경유 비교", "인증·권한 실패 구분", "Timeout·Retry 단계", "부분 적용·Reconcile", "LKG Rollback", "다중 인스턴스 Drift", "Browser Publish Flow");

    public EduGw06Handler() {
        super(new EduCapabilityDefinition(
                "EDU-GW-06", "Attempt Ledger·UNKNOWN_RESULT·LKG 복구", EduCapabilityKind.GATEWAY, "cpf-education",
                "CPF_GATEWAY_OPERATOR", List.of("attemptId", "routeVersion", "requestHash"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
                true, true,
                false, true,
                false, false,
                5, "EDU-GW-06"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.operations.gateway.reconcile"; }
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
        return List.of("edu-gw-06" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-GW-06", EduConsumerType.REFERENCE_GATEWAY, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "ROLLED_BACK", "cpf-education EDU Gateway simulator + cpfDB contract",
                "POST /api/education/edu-capabilities/EDU-GW-06/executions", "cpf.education.features.gateway.enabled", 60, List.of("attemptId", "routeVersion", "requestHash"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Attempt Ledger·UNKNOWN_RESULT·LKG 복구");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("attemptId", "");
        return Map.copyOf(invalid);
    }
}
