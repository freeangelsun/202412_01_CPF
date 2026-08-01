package com.cpf.reference.batch.tasklet.close;
import com.cpf.reference.edu.runtime.AbstractManualEduConcurrencyTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-01 ConcurrencyTest — 업무일 마감 Tasklet */
public final class EduBat01ConcurrencyTest extends AbstractManualEduConcurrencyTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
