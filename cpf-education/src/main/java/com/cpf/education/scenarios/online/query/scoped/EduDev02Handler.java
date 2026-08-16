package com.cpf.education.scenarios.online.query.scoped;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-02 — 권한·범위가 적용된 목록·상세 조회
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev02Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("RECEIVED", "VALIDATED", "APPLIED", "SUCCEEDED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("size 초과", "허용하지 않는 sort field", "최대 기간 초과", "존재하지 않는 ID", "권한 밖 조직", "DB Timeout", "부분 데이터 Source 지연");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Vendor 3종 Paging·정렬 결과 동일성", "권한별 결과 건수", "N+1 또는 과도한 Query 방지", "빈 결과·Not Found·Forbidden 구분", "조회 Timeout 시 오류 코드·Trace 확인");

    public EduDev02Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-02", "권한·범위가 적용된 목록·상세 조회", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("organizationId", "updatedAt", "customerId", "pageSize", "subjectId", "sort"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.READ_SNAPSHOT, EduWorkflowStep.PROTECT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.TIMEOUT, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
                true, false,
                false, false,
                false, false,
                3, "EDU-DEV-02"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.query.scoped"; }
    @Override public boolean readOnly() { return true; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }

    @Override
    protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // Requirement별 필수값은 공통 엔진에서 검증하고 추가 의미 검증은 하위 Hook으로 확장합니다.
    }

    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("edu-dev-02" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-02", EduConsumerType.JDBC_QUERY, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "SUCCEEDED", "cpf-education EDU fixture DB business record contract",
                "POST /api/education/edu-capabilities/EDU-DEV-02/executions", "", 60, List.of("organizationId", "updatedAt", "customerId", "pageSize", "subjectId", "sort"));
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
        invalid.put("organizationId", "");
        return Map.copyOf(invalid);
    }
}
