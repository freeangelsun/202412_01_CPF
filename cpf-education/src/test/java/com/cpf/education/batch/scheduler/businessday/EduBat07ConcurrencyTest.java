package com.cpf.education.batch.scheduler.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-07 ConcurrencyTest — 영업일 23시 Scheduler */
public final class EduBat07ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
