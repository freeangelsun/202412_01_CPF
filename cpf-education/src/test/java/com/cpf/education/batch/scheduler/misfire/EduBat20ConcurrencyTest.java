package com.cpf.education.batch.scheduler.misfire;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-20 ConcurrencyTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
