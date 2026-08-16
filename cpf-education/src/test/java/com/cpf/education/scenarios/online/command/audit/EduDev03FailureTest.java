package com.cpf.education.scenarios.online.command.audit;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-03 FailureTest — 등록·수정·상태 변경과 감사 */
public final class EduDev03FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev03Handler(); }
}
