package com.cpf.reference.batch.flow.conditional;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-11 IntegrationTest — 조건 분기·다단계 Job Flow */
public final class EduBat11IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat11Handler(); }
}
