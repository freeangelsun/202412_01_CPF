package com.cpf.education.batch.scheduler.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduFailureTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-07 FailureTest — 영업일 23시 Scheduler */
public final class EduBat07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
