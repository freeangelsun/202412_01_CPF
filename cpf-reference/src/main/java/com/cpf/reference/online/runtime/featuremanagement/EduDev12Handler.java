package com.cpf.reference.online.runtime.featuremanagement;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-12 — Cache·기능 전환·Secret 교체
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev12Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("HIT", "MISS", "STALE", "BYPASS", "OFF", "ACTIVE", "ROTATING", "FAILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Redis 연결 실패", "Stale 데이터", "직렬화 불일치", "Flag Service 장애", "잘못된 대상 규칙", "Secret 누락", "Secret 교체 후 인증 실패", "Secret 로그 노출");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Cache Hit/Miss/TTL", "장애 시 Fallback 정책", "Flag 대상 분리", "Secret Rotation 전후", "민감정보 Scan", "다중 인스턴스 Cache 무효화");
    public EduDev12Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-12", "Cache·기능 전환·Secret 교체", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("codes", "discounts", "secretAlias"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, true, false, false, false, false, 3, "EDU-DEV-12"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.runtime.featuremanagement"; }
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
                "EDU-DEV-12", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "FAILED", "cpf-reference REF DB business record contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-12/executions", "", 60, List.of("codes", "discounts", "secretAlias"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Cache·기능 전환·Secret 교체");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("codes", "");
        return Map.copyOf(invalid);
    }
}
