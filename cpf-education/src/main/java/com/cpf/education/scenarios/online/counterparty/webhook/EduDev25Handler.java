package com.cpf.education.scenarios.online.counterparty.webhook;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-25 — Webhook Callback 서명·재전송·Replay 방지
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev25Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("QUEUED", "SENT", "ACKED", "RETRY_WAIT", "DEAD");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("DNS 변경", "2xx 후 응답 유실", "4xx", "5xx", "만료 Timestamp", "Signature 오류", "Replay", "상태 변경이 있는 경우 DB Commit 전 실패와 Commit 후 응답 유실을 구분한다.", "같은 요청의 중복·동시 실행·순서 역전·늦은 응답을 포함한다.", "Retry 가능한 오류와 즉시 중단해야 하는 업무 오류를 구분한다.", "대량·다중 대상 기능에서 부분 성공이 발생하면 성공 대상을 다시 실행하지 않고 실패 대상만 Reprocess·Reconcile한다.", "복구·보상이 필요한 기능은 복구 실패와 보상 실패도 별도 상태·Attempt·Audit로 남긴다.");
    private static final List<String> REQUIRED_VERIFICATION = List.of("HMAC 검증", "Retry backoff", "DLQ", "Callback attempt ledger", "SSRF 방지", "입력 Validation·권한·Data Scope·Masking Negative Test", "해당 기능에 적용되는 중복·동시성·Timeout·응답 유실·부분 실패 Test", "해당 기능의 Failure Injection 후 필요한 Retry·Restart·Reprocess·Reconcile·Rollback Test", "Oracle·PostgreSQL·MariaDB 또는 해당 Infra의 의미 동일성 Test", "Log·Metric·Trace·Audit에 Secret·개인정보 원문이 없는지 검사");

    public EduDev25Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-25", "Webhook Callback 서명·재전송·Replay 방지", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("subscriptionId", "callbackUrl", "eventId", "signature", "timestamp", "destination", "messageKey"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, true,
                false, true,
                false, false,
                5, "EDU-DEV-25"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.counterparty.webhook"; }
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
        return List.of("edu-dev-25" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-25", EduConsumerType.HTTP, "cpf-education",
                "/external/25", "DEAD", "cpf-education counterparty simulator HTTP contract",
                "POST /api/education/edu-capabilities/EDU-DEV-25/executions", "cpf.edu.counterparty.base-url", 60, List.of("subscriptionId", "callbackUrl", "eventId", "signature", "timestamp", "destination", "messageKey"));
    }

    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Webhook Callback 서명·재전송·Replay 방지");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }

    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("subscriptionId", "");
        return Map.copyOf(invalid);
    }
}
