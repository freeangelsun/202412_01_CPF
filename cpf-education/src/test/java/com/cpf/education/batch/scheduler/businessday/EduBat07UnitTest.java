package com.cpf.education.batch.scheduler.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-07 UnitTest — 영업일 23시 Scheduler */
public final class EduBat07UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
