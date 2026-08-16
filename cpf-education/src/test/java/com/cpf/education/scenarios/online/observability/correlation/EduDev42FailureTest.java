package com.cpf.education.scenarios.online.observability.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-42 FailureTest — 로그·Metric·Trace 상관관계와 Sampling */
public final class EduDev42FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev42Handler(); }
}
