package com.cpf.reference.online.file.attachment;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-08 — 파일 업로드·검사·첨부·다운로드
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev08Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UPLOADING", "UPLOADED", "SCANNING", "AVAILABLE", "REJECTED", "FAILED", "EXPIRED", "DELETED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("허용하지 않는 확장자", "MIME 위조", "Chunk 누락", "업로드 중단", "Hash 불일치", "검사 실패/Timeout", "Storage 장애", "권한 밖 Download", "만료 파일");
    private static final List<String> REQUIRED_VERIFICATION = List.of("대용량 Streaming", "중단 후 Resume 또는 정리", "Hash 검증", "검사 상태별 접근 제한", "동시 첨부 연결", "다운로드 감사·Masking");
    public EduDev08Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-08", "파일 업로드·검사·첨부·다운로드", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("attachmentId", "mediaType", "ownerType", "fileName", "contentLength", "checksum"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.CHECKSUM, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
            true, true, false, false, false, false, 3, "EDU-DEV-08"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.file.attachment"; }
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
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-08", EduConsumerType.FILE, "cpf-reference",
                "cpf-reference EDU file store", "DELETED", "cpf-reference safe file/checksum contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-08/executions", "cpf.edu.business-file-root", 120, List.of("attachmentId", "mediaType", "ownerType", "fileName", "contentLength", "checksum"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "파일 업로드·검사·첨부·다운로드");
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
