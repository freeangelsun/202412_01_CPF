package com.cpf.reference.online.file.bulkimport;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-17 — 대량 등록 사전검증·부분 오류 보고·재업로드
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev17Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UPLOADED", "VALIDATED", "REJECTED", "APPLIED", "PARTIAL", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("헤더 불일치", "행 중복", "DB 기존값 충돌", "100행 중 3행 오류", "적용 중 장애", "재업로드", "상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.", "같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.", "Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.", "대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.", "복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Dry Run 결과", "오류 파일", "부분 적용 금지/허용 정책", "멱등 재업로드", "감사", "입력 Validation·권한·Data Scope·Masking Negative Test", "해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test", "해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test", "Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test", "Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사");
    public EduDev17Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-17", "대량 등록 사전검증·부분 오류 보고·재업로드", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("dryRun"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PREVIEW, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, false, false, 3, "EDU-DEV-17"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.file.bulkimport"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // 필수 필드·권한·범위 검증은 공통 엔진에서 수행합니다.
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, payloadInt(command, "partitionCount", payloadInt(command, "gridSize", 4)));
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("partition" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-17", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "RECONCILED", "cpf-reference REF DB business record contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-17/executions", "", 60, List.of("dryRun"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "대량 등록 사전검증·부분 오류 보고·재업로드");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("dryRun", "");
        return Map.copyOf(invalid);
    }
}
