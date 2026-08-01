package com.cpf.reference.batch.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-22 ConcurrencyTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
