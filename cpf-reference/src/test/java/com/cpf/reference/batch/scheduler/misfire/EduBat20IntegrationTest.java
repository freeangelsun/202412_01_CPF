package com.cpf.reference.batch.scheduler.misfire;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-20 IntegrationTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
