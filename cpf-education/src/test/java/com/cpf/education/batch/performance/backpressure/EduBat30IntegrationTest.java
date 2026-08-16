package com.cpf.education.batch.performance.backpressure;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-30 IntegrationTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
