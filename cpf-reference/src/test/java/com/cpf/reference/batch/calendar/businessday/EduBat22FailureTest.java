package com.cpf.reference.batch.calendar.businessday;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-22 FailureTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
