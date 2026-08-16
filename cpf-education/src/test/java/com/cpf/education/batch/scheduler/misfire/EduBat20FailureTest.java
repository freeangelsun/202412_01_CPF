package com.cpf.education.batch.scheduler.misfire;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-20 FailureTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
