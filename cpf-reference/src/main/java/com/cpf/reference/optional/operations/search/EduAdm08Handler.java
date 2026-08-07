package com.cpf.reference.optional.operations.search;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-ADM-08 — 권한·데이터 범위·Masking·사유 입력 연동
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduAdm08Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UNMASK_REQUESTED", "APPROVED", "AUTHORIZED", "MASKED", "DENIED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("권한 변경 직후", "scope 누락", "원문 Export", "reason 길이", "승인 만료", "상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.", "같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.", "Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.", "대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.", "복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Backend authorization", "browser role matrix", "audit", "IDOR", "입력 Validation·권한·Data Scope·Masking Negative Test", "해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test", "해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test", "Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test", "Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사");
    public EduAdm08Handler() {
        super(new EduCapabilityDefinition(
            "EDU-ADM-08", "권한·데이터 범위·Masking·사유 입력 연동", EduCapabilityKind.OPERATIONS, "cpf-reference", "CPF_REFERENCE_PLATFORM_OPERATOR",
            List.of("permission", "reason", "approvalId", "organizationId", "subjectId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, false, false, 3, "EDU-ADM-08"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.operations.search"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        String organizationId = String.valueOf(command.payload().get("organizationId"));
        String scope = command.dataScope();
        if (scope.startsWith("ORG:") && !scope.equals("ORG:" + organizationId)) {
            throw new EduAuthorizationException("organization scope mismatch");
        }
        if (!scope.equals("*") && !scope.equals("GLOBAL") && !scope.startsWith("ORG:")
                && !scope.equals(organizationId)) {
            throw new EduAuthorizationException("organization scope mismatch");
        }
        String permission = String.valueOf(command.payload().get("permission")).toUpperCase(Locale.ROOT);
        if (permission.contains("RAW") && command.requestReason().trim().length() < 10) {
            throw new EduValidationException("raw data access requires a concrete reason");
        }
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("organization:" + command.payload().get("organizationId")
                + ":subject:" + command.payload().get("subjectId"));
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-ADM-08", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "DENIED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-ADM-08/executions", "cpf.reference.features.operations.enabled", 60, List.of("permission", "reason", "approvalId", "organizationId", "subjectId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        String permission = String.valueOf(command.payload().get("permission")).toUpperCase(Locale.ROOT);
        boolean rawAllowed = permission.contains("RAW");
        result.put("scenarioTitle", "권한·데이터 범위·Masking·사유 입력 연동");
        result.put("businessState", rawAllowed ? "AUTHORIZED" : "MASKED");
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", false);
        result.put("organizationId", command.payload().get("organizationId"));
        result.put("maskedSubjectId", mask(String.valueOf(command.payload().get("subjectId"))));
        result.put("rawValueVisible", rawAllowed);
        result.put("rawExportRequiresApproval", true);
        result.put("idorScopeEnforced", true);
        result.put("reasonRecorded", true);
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("permission", "");
        return Map.copyOf(invalid);
    }

    private static String mask(String value) {
        if (value == null || value.isBlank()) return "***";
        int visible = Math.min(2, value.length());
        return "*".repeat(Math.max(3, value.length() - visible)) + value.substring(value.length() - visible);
    }
}
