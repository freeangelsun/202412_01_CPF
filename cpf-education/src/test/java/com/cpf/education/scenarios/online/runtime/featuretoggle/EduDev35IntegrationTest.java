package com.cpf.education.scenarios.online.runtime.featuretoggle;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-35 IntegrationTest — 기능 전환 Canary·Kill Switch·사용자 Segment */
public final class EduDev35IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev35Handler(); }
}
