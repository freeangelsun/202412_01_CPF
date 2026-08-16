package com.cpf.education.batch.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduRecoveryTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-22 RecoveryTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22RecoveryTest extends AbstractManualEduRecoveryTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
