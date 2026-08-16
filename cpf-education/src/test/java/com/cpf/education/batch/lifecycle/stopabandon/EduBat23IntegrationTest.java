package com.cpf.education.batch.lifecycle.stopabandon;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-23 IntegrationTest — Stop·Abandon·Restart 의미 분리 */
public final class EduBat23IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat23Handler(); }
}
