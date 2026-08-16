package com.cpf.education.operations.platform.configuration.reconcile;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-OPS-09 IntegrationTest — 설정 변경 Partial Apply·Reconcile */
public final class EduOps09IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduOps09Handler(); }
}
