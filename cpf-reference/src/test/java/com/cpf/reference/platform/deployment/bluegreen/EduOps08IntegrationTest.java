package com.cpf.reference.platform.deployment.bluegreen;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-08 IntegrationTest — Blue-Green·Canary 전환·되돌리기 */
public final class EduOps08IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps08Handler(); }
}
