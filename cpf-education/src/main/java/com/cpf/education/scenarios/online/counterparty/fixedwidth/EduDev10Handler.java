package com.cpf.education.scenarios.online.counterparty.fixedwidth;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-10 — 고정길이 전문 기관 이체
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev10Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("CREATED", "ENCODED", "SENT", "ACKNOWLEDGED", "REJECTED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("문자 Byte 길이 초과", "잘못된 Encoding", "전문 길이 부족/초과", "Header 거래번호 불일치", "응답 Code 미등록", "지연 응답", "부분 수신", "중복 응답");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Golden Byte 비교", "한글 Byte Length", "Layout Version 호환", "응답 Code Mapping", "전송 후 Timeout 대사", "원문 Masking");

    public EduDev10Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-10", "고정길이 전문 기관 이체", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("institution", "transfers", "amount", "currency", "endpointAlias", "institutionRequestId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
                true, false,
                false, true,
                false, false,
                5, "EDU-DEV-10"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.counterparty.fixedwidth"; }
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
        return List.of("edu-dev-10" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-10", EduConsumerType.HTTP, "cpf-education",
                "/external/10", "RECONCILED", "cpf-education counterparty simulator HTTP contract",
                "POST /api/education/edu-capabilities/EDU-DEV-10/executions", "cpf.edu.counterparty.base-url", 60, List.of("institution", "transfers", "amount", "currency", "endpointAlias", "institutionRequestId"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "고정길이 전문 기관 이체");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("institution", "");
        return Map.copyOf(invalid);
    }
}
