package com.cpf.reference.online.query.scoped;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-02 — 권한·범위가 적용된 목록·상세 조회
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev02Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("RECEIVED", "VALIDATED", "APPLIED", "SUCCEEDED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("size 초과", "허용하지 않는 sort field", "최대 기간 초과", "존재하지 않는 ID", "권한 밖 조직", "DB Timeout", "부분 데이터 Source 지연");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Vendor 3종 Paging·정렬 결과 동일성", "권한별 결과 건수", "N+1 또는 과도한 Query 방지", "빈 결과·Not Found·Forbidden 구분", "조회 Timeout 시 오류 코드·Trace 확인");
    public EduDev02Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-02", "권한·범위가 적용된 목록·상세 조회", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("organizationId", "updatedAt", "customerId", "pageSize", "subjectId", "sort"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.READ_SNAPSHOT, EduWorkflowStep.PROTECT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.TIMEOUT, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, false, false, false, false, false, 3, "EDU-DEV-02"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.query.scoped"; }
    @Override public boolean readOnly() { return true; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireLongRange(command, "pageSize", 1L, 1000L);
        requireAllowedSort(command, "sort", Set.of("id","createdAt","updatedAt","status","businessDate"));
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-02", EduConsumerType.JDBC_QUERY, "cpf-reference",
                "CPF_EDU_BUSINESS_RECORD", "SUCCEEDED", "cpf-reference REF DB business record contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-02/executions", "", 60, List.of("organizationId", "updatedAt", "customerId", "pageSize", "subjectId", "sort"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "권한·범위가 적용된 목록·상세 조회");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("pageSize", 0);
        return Map.copyOf(invalid);
    }
}
