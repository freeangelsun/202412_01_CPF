package com.cpf.reference.batch.flow.conditional;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-11 ConcurrencyTest — 조건 분기·다단계 Job Flow */
public final class EduBat11ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
