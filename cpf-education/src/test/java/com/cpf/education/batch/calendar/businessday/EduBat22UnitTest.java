package com.cpf.education.batch.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-22 UnitTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
