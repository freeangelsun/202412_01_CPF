package com.cpf.education.batch.instance.parameter;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-14 UnitTest — JobParameter 식별·중복 실행·새 Instance */
public final class EduBat14UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat14Handler(); }
}
