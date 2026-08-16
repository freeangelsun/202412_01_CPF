package com.cpf.education.batch.scheduler.misfire;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-20 RecoveryTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
