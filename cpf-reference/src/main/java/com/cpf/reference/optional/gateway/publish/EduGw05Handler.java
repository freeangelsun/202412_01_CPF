package com.cpf.reference.optional.gateway.publish;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-GW-05 — Draft·검증·승인·게시·부분 적용
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduGw05Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "VALIDATED", "APPROVAL_REQUESTED", "APPROVED", "PUBLISHING", "APPLIED", "PARTIAL", "REJECTED", "ROLLED_BACK");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("NACK", "일부 ACK", "승인 만료", "Checksum 불일치", "게시 응답 유실");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Target 직접/경유 비교", "인증·권한 실패 구분", "Timeout·Retry 단계", "부분 적용·Reconcile", "LKG Rollback", "다중 인스턴스 Drift", "Browser Publish Flow");
    public EduGw05Handler() {
        super(new EduCapabilityDefinition(
            "EDU-GW-05", "Draft·검증·승인·게시·부분 적용", EduCapabilityKind.GATEWAY, "cpf-reference", "CPF_REFERENCE_GATEWAY_OPERATOR",
            List.of("routeVersion", "checksum", "approvalId", "approvalPolicyId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.CHECKSUM, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.COMPENSATION_READY, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, true, true, false, 5, "EDU-GW-05"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.gateway.publish"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireSha256(command, "checksum");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, payloadInt(command, "memberCount", 2));
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("gateway-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-GW-05", EduConsumerType.REFERENCE_GATEWAY, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "ROLLED_BACK", "cpf-reference REF Gateway simulator + refDB contract",
                "POST /api/reference/edu-capabilities/EDU-GW-05/executions", "cpf.reference.features.gateway.enabled", 60, List.of("routeVersion", "checksum", "approvalId", "approvalPolicyId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Draft·검증·승인·게시·부분 적용");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("checksum", "not-a-sha256");
        return Map.copyOf(invalid);
    }
}
