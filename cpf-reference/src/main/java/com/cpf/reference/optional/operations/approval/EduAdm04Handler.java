package com.cpf.reference.optional.operations.approval;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-ADM-04 — 승인 필요한 위험 조치
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduAdm04Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("REQUESTED", "ACCEPTED", "RUNNING", "SUCCEEDED", "FAILED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("자기 승인", "승인 만료", "승인 범위 초과", "대상 변경");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Backend Contract Test", "Same-JVM/Remote Adapter Test", "권한·Masking Test", "Timeout·응답 유실 Test", "Browser Test", "Audit·Trace 연결");
    public EduAdm04Handler() {
        super(new EduCapabilityDefinition(
            "EDU-ADM-04", "승인 필요한 위험 조치", EduCapabilityKind.OPERATIONS, "cpf-reference", "CPF_REFERENCE_PLATFORM_OPERATOR",
            List.of("businessId", "approvalId", "approvalPolicyId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
            true, true, false, false, false, false, 3, "EDU-ADM-04"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.operations.approval"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        Object requestedBy = command.payload().get("requestedBy");
        Object approvedBy = command.payload().get("approvedBy");
        if (requestedBy != null && approvedBy != null
                && String.valueOf(requestedBy).equals(String.valueOf(approvedBy))) {
            throw new EduAuthorizationException("requester and approver must be different");
        }
        Object expiresAt = command.payload().get("approvalExpiresAtEpochMillis");
        if (expiresAt != null) {
            try {
                if (Long.parseLong(String.valueOf(expiresAt)) <= System.currentTimeMillis()) {
                    throw new EduAuthorizationException("approval expired");
                }
            } catch (NumberFormatException e) {
                throw new EduValidationException("approvalExpiresAtEpochMillis must be numeric");
            }
        }
        String businessId = String.valueOf(command.payload().get("businessId"));
        Object approvedBusinessId = command.payload().get("approvedBusinessId");
        if (approvedBusinessId != null && !businessId.equals(String.valueOf(approvedBusinessId))) {
            throw new EduAuthorizationException("approval scope does not match businessId");
        }
        Object approvedVersion = command.payload().get("approvedTargetVersion");
        if (approvedVersion != null) {
            try {
                if (Long.parseLong(String.valueOf(approvedVersion)) != command.expectedVersion()) {
                    throw new EduValidationException("approved target version changed");
                }
            } catch (NumberFormatException e) {
                throw new EduValidationException("approvedTargetVersion must be numeric");
            }
        }
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("approval:" + command.payload().get("approvalId")
                + ":business:" + command.payload().get("businessId"));
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-ADM-04", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "RECONCILED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-ADM-04/executions", "cpf.reference.features.operations.enabled", 60, List.of("businessId", "approvalId", "approvalPolicyId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "승인 필요한 위험 조치");
        result.put("businessState", "RECONCILED");
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", false);
        result.put("approvalId", command.payload().get("approvalId"));
        result.put("approvalPolicyId", command.payload().get("approvalPolicyId"));
        result.put("separationOfDutiesEnforced", true);
        result.put("approvalScopeBound", true);
        result.put("approvalTargetVersionBound", true);
        result.put("blindReplayAllowed", false);
        result.put("unknownResultRequiresReconcile", true);
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("businessId", "");
        return Map.copyOf(invalid);
    }
}
