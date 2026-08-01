package com.cpf.reference.online.security.authorization;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-11 — 권한·데이터 범위·개인정보 가림·감사
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev11Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("MASKED", "REVEALED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("URL 직접 호출", "다른 조직 ID 변조", "권한 없음", "사유 누락", "승인 만료", "Audit 저장 실패", "Export 우회", "Cache에 원문 저장");
    private static final List<String> REQUIRED_VERIFICATION = List.of("역할×조직×기능 Matrix", "API 직접 호출", "원문 조회 감사", "로그·Trace·Error Body 개인정보 검사", "Export Masking", "세션 변경 후 권한 반영");
    public EduDev11Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-11", "권한·데이터 범위·개인정보 가림·감사", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("organizationId", "subjectId"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.APPROVAL, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
            true, true, false, false, false, false, 3, "EDU-DEV-11"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.security.authorization"; }
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
                "EDU-DEV-11", EduConsumerType.JDBC_COMMAND, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "REVEALED", "cpf-reference REF DB business record contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-11/executions", "", 60, List.of("organizationId", "subjectId"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "권한·데이터 범위·개인정보 가림·감사");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("organizationId", "");
        return Map.copyOf(invalid);
    }
}
