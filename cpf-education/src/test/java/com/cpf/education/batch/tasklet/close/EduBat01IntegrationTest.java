package com.cpf.education.batch.tasklet.close;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-01 IntegrationTest — 업무일 마감 Tasklet */
public final class EduBat01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat01Handler(); }
}
