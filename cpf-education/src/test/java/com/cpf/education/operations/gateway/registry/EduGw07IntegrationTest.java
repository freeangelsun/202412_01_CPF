package com.cpf.education.operations.gateway.registry;

import com.cpf.education.verification.runtime.AbstractManualEduIntegrationTest;
import com.cpf.education.operations.runtime.application.AbstractEduCapabilityHandler;

/** EDU-GW-07 IntegrationTest — Service Discovery·Target Failover·복귀 */
public final class EduGw07IntegrationTest extends AbstractManualEduIntegrationTest {
    @Override protected AbstractEduCapabilityHandler handler() { return new EduGw07Handler(); }
}
