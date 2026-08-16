package com.cpf.education.scenarios.online.observability.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-42 ConcurrencyTest — 로그·Metric·Trace 상관관계와 Sampling */
public final class EduDev42ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev42Handler(); }
}
