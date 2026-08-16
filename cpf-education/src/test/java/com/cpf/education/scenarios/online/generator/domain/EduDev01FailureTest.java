package com.cpf.education.scenarios.online.generator.domain;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-01 FailureTest — Generator 기반 신규 업무 영역 생성 */
public final class EduDev01FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev01Handler(); }
}
