package com.cpf.reference.online.messaging.outboxinbox;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-07 — Kafka Outbox·Inbox·중복 소비·재처리
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev07Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("NEW", "PUBLISHED", "RECEIVED", "APPLIED", "FAILED", "DLQ");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("같은 Event 두 번 전달", "Publisher 중단", "Consumer Commit 전 종료", "Kafka Ack 유실", "순서 역전", "Schema Version 불일치", "Poison Message", "DLQ 재처리 중 중복");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Duplicate Event 10회 1회 반영", "Publisher/Consumer 재시작", "Outbox와 업무 저장 원자성", "Inbox와 업무 반영 원자성", "DLQ 재처리와 감사", "Topic/Group 설정 검증");
    public EduDev07Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-07", "Kafka Outbox·Inbox·중복 소비·재처리", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("eventId", "aggregateId", "aggregateVersion", "occurredAt", "payloadVersion", "destination", "messageKey"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.CHECKPOINT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
            true, false, false, true, false, false, 5, "EDU-DEV-07"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.messaging.outboxinbox"; }
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
                "EDU-DEV-07", EduConsumerType.OUTBOX, "cpf-reference",
                "CPF_EDU_OUTBOX", "DLQ", "cpf-reference durable outbox/inbox contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-07/executions", "", 60, List.of("eventId", "aggregateId", "aggregateVersion", "occurredAt", "payloadVersion", "destination", "messageKey"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Kafka Outbox·Inbox·중복 소비·재처리");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("eventId", "");
        return Map.copyOf(invalid);
    }
}
