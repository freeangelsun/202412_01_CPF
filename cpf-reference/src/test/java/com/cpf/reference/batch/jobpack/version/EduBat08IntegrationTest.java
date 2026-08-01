package com.cpf.reference.batch.jobpack.version;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-BAT-08 IntegrationTest — Job Pack Version·Artifact 배포 */
public final class EduBat08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
