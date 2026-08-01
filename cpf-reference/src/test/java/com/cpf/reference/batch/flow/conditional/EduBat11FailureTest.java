package com.cpf.reference.batch.flow.conditional;
import com.cpf.reference.edu.runtime.AbstractManualEduFailureTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-11 FailureTest — 조건 분기·다단계 Job Flow */
public final class EduBat11FailureTest extends AbstractManualEduFailureTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
