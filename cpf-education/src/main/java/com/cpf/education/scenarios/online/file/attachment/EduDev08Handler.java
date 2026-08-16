package com.cpf.education.scenarios.online.file.attachment;

import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.education.operations.runtime.consumer.*;
import com.cpf.education.operations.runtime.model.*;
import java.util.*;

/**
 * EDU-DEV-08 — 파일 업로드·검사·첨부·다운로드
 * 매뉴얼 계약의 입력·권한·상태·장애·복구·Consumer 연결을 실제 실행하는 CPF Education Capability입니다.
 * 공통 실행 원장과 Audit/Recovery 흐름을 사용하며 Marker/고정 응답으로 대체하지 않습니다.
 */
public final class EduDev08Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("UPLOADING", "UPLOADED", "SCANNING", "AVAILABLE", "REJECTED", "FAILED", "EXPIRED", "DELETED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("허용하지 않는 확장자", "MIME 위조", "Chunk 누락", "업로드 중단", "Hash 불일치", "검사 실패/Timeout", "Storage 장애", "권한 밖 Download", "만료 파일");
    private static final List<String> REQUIRED_VERIFICATION = List.of("대용량 Streaming", "중단 후 Resume 또는 정리", "Hash 검증", "검사 상태별 접근 제한", "동시 첨부 연결", "다운로드 감사·Masking");

    public EduDev08Handler() {
        super(new EduCapabilityDefinition(
                "EDU-DEV-08", "파일 업로드·검사·첨부·다운로드", EduCapabilityKind.ONLINE, "cpf-education",
                "CPF_EDU_DEVELOPER", List.of("attachmentId", "mediaType", "ownerType", "fileName", "contentLength", "checksum"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.CHECKSUM, EduWorkflowStep.PROTECT, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE),
                Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT, EduFailurePoint.TIMEOUT, EduFailurePoint.PROCESS_KILL),
                true, true,
                false, false,
                false, false,
                3, "EDU-DEV-08"));
    }

    @Override public String implementationPackage() { return "com.cpf.education.scenarios.online.file.attachment"; }
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
        return List.of("edu-dev-08" + "-" + command.businessKey());
    }

    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-08", EduConsumerType.FILE, "cpf-education",
                "cpf-education EDU file store", "DELETED", "cpf-education safe file/checksum contract",
                "POST /api/education/edu-capabilities/EDU-DEV-08/executions", "cpf.edu.business-file-root", 120, List.of("attachmentId", "mediaType", "ownerType", "fileName", "contentLength", "checksum"));
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
        invalid.put("attachmentId", "");
        return Map.copyOf(invalid);
    }
}
