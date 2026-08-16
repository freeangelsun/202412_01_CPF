package com.cpf.education.operations.gateway.ratecontrol;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-12 IntegrationTest — 다중 인스턴스 설정 Drift·Reconcile */
public final class EduGw12IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw12Handler(); }
}
