package com.cpf.reference.batch.scheduler.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduRecoveryTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-07 RecoveryTest — 영업일 23시 Scheduler */
public final class EduBat07RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat07Handler(); }
}
