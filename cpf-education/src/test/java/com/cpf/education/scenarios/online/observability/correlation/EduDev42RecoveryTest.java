package com.cpf.education.scenarios.online.observability.correlation;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-42 RecoveryTest — 로그·Metric·Trace 상관관계와 Sampling */
public final class EduDev42RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev42Handler(); }
}
