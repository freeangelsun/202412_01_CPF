package com.cpf.education.batch.scheduler.misfire;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-20 UnitTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
