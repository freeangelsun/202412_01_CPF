package com.cpf.reference.online.counterparty.rest;
import com.cpf.reference.edu.runtime.AbstractManualEduIntegrationTest;
import com.cpf.reference.edu.runtime.application.AbstractEduCapabilityHandler;
/** EDU-DEV-09 IntegrationTest — 외부 REST 신용조회와 결과 미확정 */
public final class EduDev09IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduDev09Handler(); }
}
