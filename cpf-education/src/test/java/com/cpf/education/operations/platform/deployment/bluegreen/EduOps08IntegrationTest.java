package com.cpf.education.operations.platform.deployment.bluegreen;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-08 IntegrationTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
