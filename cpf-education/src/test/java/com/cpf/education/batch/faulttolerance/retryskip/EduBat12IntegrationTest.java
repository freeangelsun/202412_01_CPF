package com.cpf.education.batch.faulttolerance.retryskip;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-12 IntegrationTest — Retry·Skip·No-Skip 예외 분류 */
public final class EduBat12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat12Handler(); }
}
