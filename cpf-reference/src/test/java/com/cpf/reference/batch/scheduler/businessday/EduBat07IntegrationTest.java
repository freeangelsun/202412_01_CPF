package com.cpf.reference.batch.scheduler.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-07 IntegrationTest — 영업일 23시 Scheduler */
public final class EduBat07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
