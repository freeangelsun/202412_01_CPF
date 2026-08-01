package com.cpf.reference.platform.deployment.rolling;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-OPS-07 IntegrationTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
