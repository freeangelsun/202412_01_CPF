package com.cpf.education.operations.gateway.servergroup;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-01 IntegrationTest — Server Group·Health·Load Balancing */
public final class EduGw01IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw01Handler(); }
}
