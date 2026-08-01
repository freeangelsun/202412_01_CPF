package com.cpf.reference.online.servicecall.topology;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-06 — 같은 애플리케이션·분리 서비스 호출 동등성
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev06Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UNKNOWN_RESULT");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("Remote 연결 실패", "응답 Timeout", "잘못된 JSON", "하위 서비스 4xx/5xx", "호출 후 응답 유실", "서비스 주소 변경", "다중 인스턴스 일부 장애");
    private static final List<String> REQUIRED_VERIFICATION = List.of("같은 Contract Test를 Local/Remote Adapter에 적용", "오류 코드·상태 동일성", "Trace Parent 전달", "시간 예산 초과", "Load Balancing과 장애 인스턴스 제외");
    public EduDev06Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-06", "같은 애플리케이션·분리 서비스 호출 동등성", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("contractId", "member"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE),
            true, false, false, true, false, false, 5, "EDU-DEV-06"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.servicecall.topology"; }
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
                "EDU-DEV-06", EduConsumerType.HTTP, "cpf-reference",
                "/external/06", "UNKNOWN_RESULT", "cpf-reference counterparty simulator HTTP contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-06/executions", "cpf.edu.counterparty.base-url", 60, List.of("contractId", "member"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "같은 애플리케이션·분리 서비스 호출 동등성");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("contractId", "");
        return Map.copyOf(invalid);
    }
}
