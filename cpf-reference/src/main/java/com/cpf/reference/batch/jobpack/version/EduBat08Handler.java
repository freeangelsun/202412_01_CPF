package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-BAT-08 — Job Pack Version·Artifact 배포
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduBat08Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("BUILT", "VALIDATED", "APPROVED", "DEPLOYED", "PARTIAL");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Checksum 불일치", "일부 Agent NACK", "이전 Version 실행");
    private static final List<String> REQUIRED_VERIFICATION = List.of("정상 실행", "중간 실패·Process 종료", "중복 Trigger", "Stop·Restart", "업무 건수·금액·Hash 대사", "ADM Execution ID 연결");
    public EduBat08Handler() {
        super(new EduCapabilityDefinition(
            "EDU-BAT-08", "Job Pack Version·Artifact 배포", EduCapabilityKind.BATCH, "cpf-reference", "CPF_REFERENCE_BATCH_OPERATOR",
            List.of("jobPackId", "artifactSha", "fileName", "contentLength", "checksum"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.CLAIM_LEASE, EduWorkflowStep.CHECKSUM, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.CHECKPOINT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL, EduFailurePoint.LEASE_LOST),
            true, true, true, true, false, false, 5, "EDU-BAT-08"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.batch.jobpack.version"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireLongRange(command, "contentLength", 0L, 2147483647L);
        requireSha256(command, "checksum");
        requireSafePath(command, "fileName");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-BAT-08", EduConsumerType.SPRING_BATCH, "cpf-reference",
                "eduBat08Job", "PARTIAL", "cpf-reference optional Spring Batch Job/Step contract",
                "POST /api/reference/edu-capabilities/EDU-BAT-08/executions", "cpf.reference.features.batch.enabled", 3600, List.of("jobPackId", "artifactSha", "fileName", "contentLength", "checksum"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Job Pack Version·Artifact 배포");
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
