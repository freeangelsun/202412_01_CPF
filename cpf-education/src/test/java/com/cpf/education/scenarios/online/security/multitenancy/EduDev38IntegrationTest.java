package com.cpf.education.scenarios.online.security.multitenancy;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-38 IntegrationTest — 다중 Tenant 격리·설정·데이터 범위 */
public final class EduDev38IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev38Handler(); }
}
