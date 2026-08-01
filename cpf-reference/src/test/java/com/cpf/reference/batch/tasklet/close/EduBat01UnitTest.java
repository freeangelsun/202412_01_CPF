package com.cpf.reference.batch.tasklet.close;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-01 UnitTest — 업무일 마감 Tasklet */
public final class EduBat01UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
