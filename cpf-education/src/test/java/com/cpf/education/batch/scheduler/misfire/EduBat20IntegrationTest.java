package com.cpf.education.batch.scheduler.misfire;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-20 IntegrationTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
