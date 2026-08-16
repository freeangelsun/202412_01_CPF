package com.cpf.education.batch.tasklet.close;

import com.cpf.education.verification.runtime.AbstractManualEduUnitTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-01 UnitTest — 업무일 마감 Tasklet */
public final class EduBat01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
