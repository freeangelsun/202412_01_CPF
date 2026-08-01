package com.cpf.reference.optional.backoffice.policysimulation;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-BZA-03 — 결재정책 Version·경로 사전 계산
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduBackoffice03Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "WITHDRAWN", "CANCELLED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("정책 없음", "다중 정책 충돌", "미래 Version", "조직장 부재");
    private static final List<String> REQUIRED_VERIFICATION = List.of("기준일 경계", "유효기간 겹침", "권한 Matrix", "Version 충돌", "응답 유실 대사", "Browser 메뉴 Field·Button", "감사·Download 권한");
    public EduBackoffice03Handler() {
        super(new EduCapabilityDefinition(
            "EDU-BZA-03", "결재정책 Version·경로 사전 계산", EduCapabilityKind.BACKOFFICE, "cpf-reference", "CPF_REFERENCE_BACKOFFICE_OPERATOR",
            List.of("policyVersion", "businessType", "amount", "organizationId", "approvalPolicyId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PREVIEW, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, false, false, 3, "EDU-BZA-03"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.backoffice.policysimulation"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireDecimalNonNegative(command, "amount");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BZA-03", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "CANCELLED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-BZA-03/executions", "cpf.reference.features.backoffice.enabled", 60, List.of("policyVersion", "businessType", "amount", "organizationId", "approvalPolicyId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "결재정책 Version·경로 사전 계산");
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
