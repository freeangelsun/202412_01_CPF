package com.cpf.reference.batch.performance.backpressure;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-30 IntegrationTest — 대용량 처리 성능·용량·Backpressure */
public final class EduBat30IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat30Handler(); }
}
