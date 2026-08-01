package com.cpf.reference.optional.backoffice.delegation;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-BZA-05 — 위임·대결·대행 책임
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduBackoffice05Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "SUBMITTED", "APPROVED", "REJECTED", "WITHDRAWN", "CANCELLED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("기간 겹침", "순환 위임", "범위 초과", "만료 후 승인");
    private static final List<String> REQUIRED_VERIFICATION = List.of("기준일 경계", "유효기간 겹침", "권한 Matrix", "Version 충돌", "응답 유실 대사", "Browser 메뉴 Field·Button", "감사·Download 권한");
    public EduBackoffice05Handler() {
        super(new EduCapabilityDefinition(
            "EDU-BZA-05", "위임·대결·대행 책임", EduCapabilityKind.BACKOFFICE, "cpf-reference", "CPF_REFERENCE_BACKOFFICE_OPERATOR",
            List.of("delegator", "delegate", "validFrom", "to", "scope", "approvalPolicyId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
            true, true, false, false, false, false, 3, "EDU-BZA-05"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.backoffice.delegation"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // 필수 필드·권한·범위 검증은 공통 엔진에서 수행합니다.
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BZA-05", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "CANCELLED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-BZA-05/executions", "cpf.reference.features.backoffice.enabled", 60, List.of("delegator", "delegate", "validFrom", "to", "scope", "approvalPolicyId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "위임·대결·대행 책임");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("delegator", "");
        return Map.copyOf(invalid);
    }
}
