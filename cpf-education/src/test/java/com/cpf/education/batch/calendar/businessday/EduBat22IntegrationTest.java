package com.cpf.education.batch.calendar.businessday;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-22 IntegrationTest — 휴일 Calendar·영업일 순번 JobParameter */
public final class EduBat22IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat22Handler(); }
}
