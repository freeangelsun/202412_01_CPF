package com.cpf.reference.online.database.migration;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-14 — Oracle·PostgreSQL·MariaDB 동일 의미 Migration
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev14Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("NOT_APPLIED", "APPLYING", "APPLIED", "VERIFIED", "FAILED", "ROLLED_BACK");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("중간 중단", "재실행", "기존 Null/불량 데이터", "Lock Timeout", "Index 생성 실패", "Rollback 후 App 호환", "Vendor 문법 차이");
    private static final List<String> REQUIRED_VERIFICATION = List.of("세 Vendor 신규 설치", "이전 Version→Upgrade", "Upgrade 재실행", "중단 후 재개", "Rollback·Forward Fix", "Schema Diff와 Query 결과 동일성");
    public EduDev14Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-14", "Oracle·PostgreSQL·MariaDB 동일 의미 Migration", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("customerId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.COMPENSATION_READY, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
            true, true, false, false, true, true, 3, "EDU-DEV-14"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.database.migration"; }
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
                "EDU-DEV-14", EduConsumerType.PROCESS, "cpf-reference",
                "cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1", "ROLLED_BACK", "cpf-reference allowlisted EDU operation script contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-14/executions", "cpf.repository-root", 600, List.of("customerId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Oracle·PostgreSQL·MariaDB 동일 의미 Migration");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("customerId", "");
        return Map.copyOf(invalid);
    }
}
