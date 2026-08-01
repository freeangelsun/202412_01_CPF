package com.cpf.reference.batch.scheduler.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-07 FailureTest — 영업일 23시 Scheduler */
public final class EduBat07FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
