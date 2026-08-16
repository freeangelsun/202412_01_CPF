package com.cpf.education.batch.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-22 ConcurrencyTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
