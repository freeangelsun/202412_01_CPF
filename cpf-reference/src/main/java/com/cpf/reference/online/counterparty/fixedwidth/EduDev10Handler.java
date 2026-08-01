package com.cpf.reference.online.counterparty.fixedwidth;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-10 — 고정길이 전문 기관 이체
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev10Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("CREATED", "ENCODED", "SENT", "ACKNOWLEDGED", "REJECTED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("문자 Byte 길이 초과", "잘못된 Encoding", "전문 길이 부족/초과", "Header 거래번호 불일치", "응답 Code 미등록", "지연 응답", "부분 수신", "중복 응답");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Golden Byte 비교", "한글 Byte Length", "Layout Version 호환", "응답 Code Mapping", "전송 후 Timeout 대사", "원문 Masking");
    public EduDev10Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-10", "고정길이 전문 기관 이체", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("institution", "transfers", "amount", "currency", "endpointAlias", "institutionRequestId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, false, false, true, false, false, 5, "EDU-DEV-10"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.counterparty.fixedwidth"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireDecimalNonNegative(command, "amount");
        requireSafeEndpoint(command, "endpointAlias");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-10", EduConsumerType.HTTP, "cpf-reference",
                "/external/10", "RECONCILED", "cpf-reference counterparty simulator HTTP contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-10/executions", "cpf.edu.counterparty.base-url", 60, List.of("institution", "transfers", "amount", "currency", "endpointAlias", "institutionRequestId"));
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
        invalid.put("amount", "-1.00");
        return Map.copyOf(invalid);
    }
}
