package com.cpf.reference.batch.scheduler.misfire;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-20 FailureTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
