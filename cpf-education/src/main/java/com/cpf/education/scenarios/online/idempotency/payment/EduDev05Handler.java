package com.cpf.education.scenarios.online.idempotency.payment;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-05 — 지급 등록 멱등성·응답 유실·결과 대사
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev05Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("RECEIVED", "PROCESSING", "SUCCEEDED", "FAILED", "UNKNOWN_RESULT", "RECONCILED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("같은 Key·같은 본문", "같은 Key·다른 본문", "PROCESSING 중 재호출", "DB Commit 전 중단", "DB Commit 후 응답 유실", "외부 이체 전송 후 응답 유실", "Operation 만료 후 재호출");
    private static final List<String> REQUIRED_VERIFICATION = List.of("원장 1건 보장", "요청 Hash 충돌", "동시 10회 같은 Key", "Failure Point별 상태", "대사 후 UNKNOWN_RESULT 해소", "로그·Trace·감사에 같은 operationId");

    public EduDev05Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-05", "지급 등록 멱등성·응답 유실·결과 대사", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("customerId", "amount", "currency"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.CHECKSUM, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, true,
                false, false,
                false, false,
                3, "EDU-DEV-05"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.idempotency.payment"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }

    @Override
    protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
        // Requirement별 필수값은 공통 엔진에서 검증하고 추가 의미 검증은 하위 Hook으로 확장합니다.
    }

    @Override public List<String> targetKeys(EduExecutionCommand command) {
        return List.of("edu-dev-05" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-05", EduConsumerType.JDBC_COMMAND, "cpf-education",
                "CPF_EDU_BUSINESS_RECORD", "RECONCILED", "cpf-education EDU fixture DB business record contract",
                "POST /api/education/edu-capabilities/EDU-DEV-05/executions", "", 60, List.of("customerId", "amount", "currency"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "지급 등록 멱등성·응답 유실·결과 대사");
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
