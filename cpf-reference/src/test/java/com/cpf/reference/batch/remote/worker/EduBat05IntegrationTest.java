package com.cpf.reference.batch.remote.worker;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-05 IntegrationTest — Manager·Worker·Lease·Fencing */
public final class EduBat05IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat05Handler(); }
}
