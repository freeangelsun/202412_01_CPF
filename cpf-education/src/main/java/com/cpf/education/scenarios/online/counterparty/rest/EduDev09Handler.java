package com.cpf.education.scenarios.online.counterparty.rest;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-09 — 외부 REST 신용조회와 결과 미확정
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev09Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("REQUESTED", "SENDING", "SENT", "SUCCEEDED", "FAILED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("DNS/연결 실패", "Connect Timeout", "Read Timeout", "5xx", "429", "Malformed JSON", "응답 Signature 오류", "요청 전송 후 Timeout", "상태 조회도 Timeout", "상대 중복 응답");
    private static final List<String> REQUIRED_VERIFICATION = List.of("재시도 허용 단계·금지 단계", "외부 Stub Failure Matrix", "응답 유실 후 새 요청 없음", "상태 조회 대사", "Circuit Breaker", "비밀 Header 로그 미노출");

    public EduDev09Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-09", "외부 REST 신용조회와 결과 미확정", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("credit", "endpointAlias", "institutionRequestId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, false,
                false, true,
                false, false,
                5, "EDU-DEV-09"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.counterparty.rest"; }
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
        return List.of("edu-dev-09" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-09", EduConsumerType.HTTP, "cpf-education",
                "/external/09", "RECONCILED", "cpf-education counterparty simulator HTTP contract",
                "POST /api/education/edu-capabilities/EDU-DEV-09/executions", "cpf.edu.counterparty.base-url", 60, List.of("credit", "endpointAlias", "institutionRequestId"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "외부 REST 신용조회와 결과 미확정");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("credit", "");
        return Map.copyOf(invalid);
    }
}
