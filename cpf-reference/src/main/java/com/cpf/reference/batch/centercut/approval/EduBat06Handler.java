package com.cpf.reference.batch.centercut.approval;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-BAT-06 — 센터컷 Preview·승인·실행
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduBat06Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("DRAFT", "PREVIEWED", "APPROVED", "RUNNING", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("승인 후 대상 변경", "Checksum 불일치", "일부 실패");
    private static final List<String> REQUIRED_VERIFICATION = List.of("정상 실행", "중간 실패·Process 종료", "중복 Trigger", "Stop·Restart", "업무 건수·금액·Hash 대사", "ADM Execution ID 연결");
    public EduBat06Handler() {
        super(new EduCapabilityDefinition(
            "EDU-BAT-06", "센터컷 Preview·승인·실행", EduCapabilityKind.BATCH, "cpf-reference", "CPF_REFERENCE_BATCH_OPERATOR",
            List.of("criteriaVersion", "snapshotId", "checksum", "approvalPolicyId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PREVIEW, EduWorkflowStep.APPROVAL, EduWorkflowStep.CLAIM_LEASE, EduWorkflowStep.CHECKSUM, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.CHECKPOINT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL, EduFailurePoint.LEASE_LOST),
            true, true, true, false, false, false, 5, "EDU-BAT-06"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.batch.centercut.approval"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireSha256(command, "checksum");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BAT-06", EduConsumerType.SPRING_BATCH, "cpf-reference",
                "eduBat06Job", "RECONCILED", "cpf-reference optional Spring Batch Job/Step contract",
                "POST /api/reference/edu-capabilities/EDU-BAT-06/executions", "cpf.reference.features.batch.enabled", 3600, List.of("criteriaVersion", "snapshotId", "checksum", "approvalPolicyId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "센터컷 Preview·승인·실행");
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
