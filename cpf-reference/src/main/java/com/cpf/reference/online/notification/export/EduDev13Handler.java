package com.cpf.reference.online.notification.export;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-13 — 알림·비동기 내보내기·다운로드 감사
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev13Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("REQUESTED", "GENERATING", "AVAILABLE", "FAILED", "EXPIRED", "PENDING", "SENT");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("대량 데이터 Memory 초과", "생성 중 Process 종료", "Storage 실패", "알림 실패", "만료 Token", "권한 없음", "동일 요청 중복", "부분 파일 노출");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Streaming Export", "Restart/재생성 정책", "Hash·건수 대사", "알림 재시도", "Token 만료", "다운로드 감사", "개인정보 Masking");
    public EduDev13Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-13", "알림·비동기 내보내기·다운로드 감사", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("settlements", "fileName", "contentLength", "checksum", "destination", "messageKey"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.CHECKSUM, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.OUTBOX, EduWorkflowStep.EXTERNAL_SEND, EduWorkflowStep.ACK, EduWorkflowStep.RECONCILE, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.BEFORE_EXTERNAL_SEND, EduFailurePoint.AFTER_EXTERNAL_SEND, EduFailurePoint.RESPONSE_LOST, EduFailurePoint.PARTIAL_TARGET_FAILURE, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
            true, false, false, true, false, false, 5, "EDU-DEV-13"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.notification.export"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireLongRange(command, "contentLength", 0L, 2147483647L);
        requireSha256(command, "checksum");
        requireSafePath(command, "fileName");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, payloadInt(command, "partitionCount", payloadInt(command, "gridSize", 4)));
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("partition" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-13", EduConsumerType.OUTBOX, "cpf-reference",
                "CPF_EDU_OUTBOX", "SENT", "cpf-reference durable outbox/inbox contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-13/executions", "", 60, List.of("settlements", "fileName", "contentLength", "checksum", "destination", "messageKey"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "알림·비동기 내보내기·다운로드 감사");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("checksum", "not-a-sha256");
        return Map.copyOf(invalid);
    }
}
