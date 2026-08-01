package com.cpf.reference.batch.scheduler.misfire;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-20 ConcurrencyTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
