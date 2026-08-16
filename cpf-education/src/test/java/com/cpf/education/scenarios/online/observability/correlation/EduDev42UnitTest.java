package com.cpf.education.scenarios.online.observability.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-42 UnitTest — 로그·Metric·Trace 상관관계와 Sampling */
public final class EduDev42UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev42Handler(); }
}
