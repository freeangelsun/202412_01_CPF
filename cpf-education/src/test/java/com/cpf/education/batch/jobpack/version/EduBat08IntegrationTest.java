package com.cpf.education.batch.jobpack.version;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-BAT-08 IntegrationTest — Job Pack Version·Artifact 배포 */
public final class EduBat08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduBat08Handler(); }
}
