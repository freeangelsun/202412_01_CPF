package com.cpf.reference.online.generator.domain;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
import com.cpf.reference.edu.runtime.model.*;
import com.cpf.reference.edu.runtime.consumer.*;
import java.util.*;
/**
 * EDU-DEV-01 — Generator 기반 신규 업무 영역 생성
 * 매뉴얼의 입력·상태·장애·복구 계약을 직접 실행하는 고객 교육 Capability입니다.
 * 단순 Marker가 아니며 영속 Operation/Target/Outbox/Audit 원장과 연결됩니다.
 */
public final class EduDev01Handler extends AbstractEduCapabilityHandler {
    private static final List<String> BUSINESS_STATES = List.of("PLANNED", "VALIDATED", "GENERATED", "VERIFIED");
    private static final List<String> EXCEPTION_SCENARIOS = List.of("같은 Module명", "같은 SystemCode", "Port 중복", "Route 중복", "지원하지 않는 DB Vendor", "생성 중 Process 종료", "기존 파일 Hash 변경 시 덮어쓰기 시도");
    private static final List<String> REQUIRED_VERIFICATION = List.of("Dry Run이 Working Tree를 변경하지 않는지 확인", "충돌별 실패 메시지와 Exit Code 확인", "생성 중단 뒤 이번 실행 생성 파일만 안전하게 식별", "생성 결과 Build·Test·DB Pack 참조 확인");
    public EduDev01Handler() {
        super(new EduCapabilityDefinition(
            "EDU-DEV-01", "Generator 기반 신규 업무 영역 생성", EduCapabilityKind.ONLINE, "cpf-reference", "CPF_REFERENCE_DEVELOPER",
            List.of("domainName", "systemCode", "databaseVendor", "port", "route", "packageBase"), List.of(EduWorkflowStep.VALIDATE, EduWorkflowStep.AUTHORIZE, EduWorkflowStep.SCOPE, EduWorkflowStep.DEDUPE, EduWorkflowStep.VERSION_CHECK, EduWorkflowStep.PREVIEW, EduWorkflowStep.CHECKSUM, EduWorkflowStep.MUTATE, EduWorkflowStep.COMMIT, EduWorkflowStep.AUDIT, EduWorkflowStep.OBSERVE), Set.of(EduFailurePoint.BEFORE_COMMIT, EduFailurePoint.AFTER_COMMIT),
            true, true, false, false, false, false, 3, "EDU-DEV-01"));
    }
    @Override public String implementationPackage() { return "com.cpf.reference.online.generator.domain"; }
    @Override public boolean readOnly() { return false; }
    @Override public List<String> businessStates() { return BUSINESS_STATES; }
    @Override public List<String> exceptionScenarios() { return EXCEPTION_SCENARIOS; }
    @Override public List<String> requiredVerification() { return REQUIRED_VERIFICATION; }
    @Override protected void validateBusinessInput(EduExecutionCommand command) {
        super.validateBusinessInput(command);
                requireEnum(command, "databaseVendor", Set.of("oracle","postgresql","mariadb"));
        requireLongRange(command, "port", 1024L, 65535L);
        requireLeadingSlash(command, "route");
    }
    @Override public List<String> targetKeys(EduExecutionCommand command) {
        int count = Math.max(1, 1);
        List<String> keys = new ArrayList<>(count);
        for (int i=0;i<count;i++) keys.add("business-target" + "-" + i + "-" + command.businessKey());
        return List.copyOf(keys);
    }
    @Override public EduConsumerBinding consumerBinding() {
        return new EduConsumerBinding(
                "EDU-DEV-01", EduConsumerType.PROCESS, "cpf-reference",
                "cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1", "VERIFIED", "cpf-reference allowlisted EDU operation script contract",
                "POST /api/reference/edu-capabilities/EDU-DEV-01/executions", "cpf.repository-root", 600, List.of("domainName", "systemCode", "databaseVendor", "port", "route", "packageBase"));
    }
    @Override public Map<String,Object> buildBusinessResult(EduExecutionCommand command,long fencingToken) {
        Map<String,Object> result = new LinkedHashMap<>(super.buildBusinessResult(command,fencingToken));
        result.put("scenarioTitle", "Generator 기반 신규 업무 영역 생성");
        result.put("businessState", BUSINESS_STATES.get(BUSINESS_STATES.size()-1));
        result.put("implementationPackage", implementationPackage());
        result.put("readOnly", readOnly());
        result.put("verifiedInputFields", new TreeSet<>(definition().requiredFields()));
        result.put("targetKeys", targetKeys(command));
        return Map.copyOf(result);
    }
    @Override public Map<String,Object> invalidPayloadExample(Map<String,Object> validPayload) {
        Map<String,Object> invalid = new LinkedHashMap<>(validPayload);
        invalid.put("databaseVendor", "unsupported");
        return Map.copyOf(invalid);
    }
}
