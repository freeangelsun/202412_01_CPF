package com.cpf.education.batch.flow.conditional;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-11 UnitTest — 조건 분기·다단계 Job Flow */
public final class EduBat11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
