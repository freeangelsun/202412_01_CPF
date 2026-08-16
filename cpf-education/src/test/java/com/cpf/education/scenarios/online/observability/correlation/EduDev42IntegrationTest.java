package com.cpf.education.scenarios.online.observability.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-42 IntegrationTest — 로그·Metric·Trace 상관관계와 Sampling */
public final class EduDev42IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev42Handler(); }
}
