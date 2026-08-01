package com.cpf.reference.batch.tasklet.close;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-01 IntegrationTest — 업무일 마감 Tasklet */
public final class EduBat01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
