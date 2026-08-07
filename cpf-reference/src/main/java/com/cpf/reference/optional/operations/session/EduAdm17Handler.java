package com.cpf.reference.optional.operations.session;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-ADM-17 — Browser 세션 만료·재로그인·위험 조치 안전성
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduAdm17Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("CONFIRMING", "SESSION_EXPIRED", "REAUTHENTICATED", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("POST 전 만료", "POST 후 응답 전 만료", "다중 Tab", "CSRF", "상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.", "같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.", "Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.", "대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.", "복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Session expiry injection", "attempt ledger", "no auto replay", "입력 Validation·권한·Data Scope·Masking Negative Test", "해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test", "해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test", "Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test", "Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사");
    public EduAdm17Handler() {
        super(new EduCapabilityDefinition(
            "EDU-ADM-17", "Browser 세션 만료·재로그인·위험 조치 안전성", EduCapabilityKind.OPERATIONS, "cpf-reference", "CPF_REFERENCE_PLATFORM_OPERATOR",
            List.of("sessionId", "commandId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, false, false, 3, "EDU-ADM-17"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.operations.session"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        if (Boolean.FALSE.equals(command.payload().get("csrfValid"))) {
            throw new EduAuthorizationException("CSRF validation failed");
        }
        if (Boolean.FALSE.equals(command.payload().get("sameOrigin"))) {
            throw new EduAuthorizationException("same-origin validation failed");
        }
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("session:" + command.payload().get("sessionId")
                + ":command:" + command.payload().get("commandId"));
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-ADM-17", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "RECONCILED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-ADM-17/executions", "cpf.reference.features.operations.enabled", 60, List.of("sessionId", "commandId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        boolean expired = Boolean.TRUE.equals(command.payload().get("sessionExpired"));
        boolean postSent = Boolean.TRUE.equals(command.payload().get("postSent"));
        boolean reauthenticated = Boolean.TRUE.equals(command.payload().get("reauthenticated"));
        String state = expired && !reauthenticated ? "SESSION_EXPIRED"
                : reauthenticated ? "REAUTHENTICATED" : "CONFIRMING";
        result.put("scenarioTitle", "Browser 세션 만료·재로그인·위험 조치 안전성");
        result.put("businessState", state);
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", false);
        result.put("sessionExpired", expired);
        result.put("reauthenticated", reauthenticated);
        result.put("autoReplayAllowed", false);
        result.put("postWasSent", postSent);
        result.put("commandReconcileRequired", expired && postSent);
        result.put("csrfEnforced", true);
        result.put("sameOriginEnforced", true);
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("sessionId", "");
        return Map.copyOf(invalid);
    }
}
