package com.cpf.education.scenarios.online.counterparty.fixedwidth;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-DEV-10 IntegrationTest — 고정길이 전문 기관 이체 */
public final class EduDev10IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev10Handler(); }
}
