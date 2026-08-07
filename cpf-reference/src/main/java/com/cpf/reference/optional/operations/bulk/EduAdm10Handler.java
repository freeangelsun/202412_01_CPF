package com.cpf.reference.optional.operations.bulk;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-ADM-10 — 대상 일괄 조치·부분 성공·결과 파일
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduAdm10Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("REQUESTED", "RUNNING", "PARTIAL", "REPROCESSING", "COMPLETED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("대상 중복", "Version 혼합", "응답 유실", "재처리 중 성공 대상 포함", "상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.", "같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.", "Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.", "대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.", "복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Target result contract", "downloadable result", "idempotency", "입력 Validation·권한·Data Scope·Masking Negative Test", "해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test", "해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test", "Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test", "Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사");
    public EduAdm10Handler() {
        super(new EduCapabilityDefinition(
            "EDU-ADM-10", "대상 일괄 조치·부분 성공·결과 파일", EduCapabilityKind.OPERATIONS, "cpf-reference", "CPF_REFERENCE_PLATFORM_OPERATOR",
            List.of("targetIds", "command", "expectedVersions", "reason", "fileName", "contentLength", "checksum"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.COMPENSATION_READY, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, true, false, 3, "EDU-ADM-10"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.optional.operations.bulk"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        requireLongRange(command, "contentLength", 0L, 2147483647L);
        requireSha256(command, "checksum");
        requireSafePath(command, "fileName");
        List<String> targets = targetIds(command);
        if (targets.isEmpty()) throw new EduValidationException("targetIds must not be empty");
        if (new LinkedHashSet<>(targets).size() != targets.size()) {
            throw new EduValidationException("targetIds must be unique");
        }
        Object versions = command.payload().get("expectedVersions");
        if (versions instanceof Map<?,?> map && map.size() != targets.size()) {
            throw new EduValidationException("expectedVersions must cover every target");
        }
        if (versions instanceof Collection<?> values && values.size() != targets.size()) {
            throw new EduValidationException("expectedVersions must cover every target");
        }
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return targetIds(command).stream().map(id -> "target:" + id).toList();
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-ADM-10", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "COMPLETED", "cpf-reference REF DB backoffice/operations contract",
                "POST /api/reference/edu-capabilities/EDU-ADM-10/executions", "cpf.reference.features.operations.enabled", 60, List.of("targetIds", "command", "expectedVersions", "reason", "fileName", "contentLength", "checksum"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        List<String> targets = targetIds(command);
        Set<String> failed = new LinkedHashSet<>(stringList(command.payload().get("failedTargetIds")));
        if (!targets.containsAll(failed)) throw new EduValidationException("failedTargetIds contains unknown target");
        List<String> succeeded = targets.stream().filter(id -> !failed.contains(id)).toList();
        List<Map<String,Object>> targetResults = new ArrayList<>();
        for (String id : targets) {
            targetResults.add(Map.of("targetId", id, "status", failed.contains(id) ? "FAILED" : "SUCCEEDED"));
        }
        result.put("scenarioTitle", "대상 일괄 조치·부분 성공·결과 파일");
        result.put("businessState", failed.isEmpty() ? "COMPLETED" : "PARTIAL");
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", false);
        result.put("targetResults", List.copyOf(targetResults));
        result.put("successfulTargetIds", succeeded);
        result.put("failedTargetIds", List.copyOf(failed));
        result.put("reprocessTargetIds", List.copyOf(failed));
        result.put("successfulTargetsReplayAllowed", false);
        result.put("downloadableResult", Map.of(
                "fileName", command.payload().get("fileName"),
                "contentLength", command.payload().get("contentLength"),
                "checksum", command.payload().get("checksum")));
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("checksum", "not-a-sha256");
        return Map.copyOf(invalid);
    }

    private static List<String> targetIds(EduExecutionCommand command) {
        return stringList(command.payload().get("targetIds"));
    }

    private static List<String> stringList(Object value) {
        if (value instanceof Collection<?> values) {
            return values.stream().map(String::valueOf).map(String::trim).filter(s -> !s.isEmpty()).toList();
        }
        if (value == null) return List.of();
        return Arrays.stream(String.valueOf(value).split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }
}
