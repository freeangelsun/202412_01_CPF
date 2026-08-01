package com.cpf.reference.batch.flow.conditional;
import com.cpf.reference.edu.runtime.AbstractManualEduUnitTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-11 UnitTest — 조건 분기·다단계 Job Flow */
public final class EduBat11UnitTest extends AbstractManualEduUnitTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
