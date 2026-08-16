package com.cpf.education.generator;

import com.cpf.education.scenarios.online.generator.domain.EduDev01Handler;
import java.util.List;

/**
 * CPF Generator Golden Path를 Education에서 실제 실행 계약으로 보여주는 진입점입니다.
 * 생성 예제의 구현 패키지와 검증 항목을 실제 EDU-DEV-01 Capability에서 조회해 문서와 실행 경로의 불일치를 방지합니다.
 */
public final class EducationGeneratorEducation {
    private final EduDev01Handler handler = new EduDev01Handler();

    /** 생성 예제가 사용하는 Canonical 구현 패키지를 반환합니다. */
    public String implementationPackage() { return handler.implementationPackage(); }

    /** 생성 후 반드시 수행할 검증 항목을 반환합니다. */
    public List<String> requiredVerification() { return handler.requiredVerification(); }

    /** 생성 흐름에서 관찰 가능한 업무 상태를 반환합니다. */
    public List<String> businessStates() { return handler.businessStates(); }
}
