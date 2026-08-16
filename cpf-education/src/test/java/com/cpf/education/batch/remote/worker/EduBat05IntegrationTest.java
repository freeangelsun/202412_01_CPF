package com.cpf.education.batch.remote.worker;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-05 IntegrationTest — Manager·Worker·Lease·Fencing */
public final class EduBat05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
