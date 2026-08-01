package com.cpf.reference.batch.scheduler.misfire;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-20 UnitTest — Scheduler Misfire·Catch-up·건너뛰기 */
public final class EduBat20UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat20Handler(); }
}
