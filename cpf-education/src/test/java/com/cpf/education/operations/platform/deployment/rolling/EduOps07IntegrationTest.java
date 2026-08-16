package com.cpf.education.operations.platform.deployment.rolling;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-07 IntegrationTest — Rolling 배포·Session·Connection Drain */
public final class EduOps07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps07Handler(); }
}
