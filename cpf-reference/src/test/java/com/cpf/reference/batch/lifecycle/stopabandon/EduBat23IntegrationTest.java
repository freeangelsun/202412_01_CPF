package com.cpf.reference.batch.lifecycle.stopabandon;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-23 IntegrationTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
