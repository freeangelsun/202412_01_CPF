package com.cpf.reference.batch.scheduler.misfire;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-20 RecoveryTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
