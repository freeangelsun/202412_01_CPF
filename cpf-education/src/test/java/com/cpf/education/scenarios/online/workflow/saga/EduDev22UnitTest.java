package com.cpf.education.scenarios.online.workflow.saga;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-22 UnitTest — 서비스 간 Saga 보상·수동 확정 */
public final class EduDev22UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev22Handler(); }
}
