package com.cpf.education.scenarios.online.resilience.recovery;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-15 FailureTest — 지급 업무 장애 주입·복구·운영 인계 */
public final class EduDev15FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev15Handler(); }
}
