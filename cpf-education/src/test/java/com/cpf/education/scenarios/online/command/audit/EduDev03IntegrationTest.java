package com.cpf.education.scenarios.online.command.audit;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-03 IntegrationTest — 등록·수정·상태 변경과 감사 */
public final class EduDev03IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev03Handler(); }
}
