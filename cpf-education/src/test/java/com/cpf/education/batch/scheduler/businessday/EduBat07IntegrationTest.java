package com.cpf.education.batch.scheduler.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-07 IntegrationTest — 영업일 23시 Scheduler */
public final class EduBat07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
